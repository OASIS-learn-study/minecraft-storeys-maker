/**
 * ch.vorburger.minecraft.storeys
 *
 * Copyright (C) 2016 - 2018 Michael Vorburger.ch <mike@vorburger.ch>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.vorburger.minecraft.storeys.simple.impl;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import ch.vorburger.minecraft.storeys.simple.TokenProvider;
import org.junit.Test;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.living.player.Player;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TokenProviderImplTest {

    public TokenProviderImplTest() {
        tokenProvider = new TokenProviderImpl(gameMock);
    }

    private final Game gameMock = mock(Game.class);

    private TokenProvider tokenProvider;

    @Test
    public void getCode() {
        //given
        final Player player = mock(Player.class);
        final String uuid = UUID.randomUUID().toString();

        //when
        when(player.getIdentifier()).thenReturn(uuid);
        final String code = tokenProvider.getCode(player);

        //then
        assertEquals(uuid, tokenProvider.login(code));
    }

    @Test
    public void getPlayer() {
        //given
        UUID uuid = UUID.randomUUID();
        Server serverMock = mock(Server.class);
        Player playerMock = mock(Player.class);

        //when
        when(gameMock.getServer()).thenReturn(serverMock);
        when(serverMock.getPlayer(uuid)).thenReturn(Optional.of(playerMock));
        Player player = tokenProvider.getPlayer(uuid.toString());

        //then
        assertEquals(player, playerMock);
    }

    @Test(expected = NotLoggedInException.class)
    public void shouldThrowWhenNotValidLogin() {
        //when
        tokenProvider.login("invalid code");
    }

    @Test(expected = NotLoggedInException.class)
    public void shouldRemoveTokensAfterTimeOut() throws InterruptedException {
        //given
        tokenProvider = new TokenProviderImpl(gameMock, 1, TimeUnit.SECONDS);
        Player playerMock = mock(Player.class);

        //when
        final String code = tokenProvider.getCode(playerMock);
        Thread.sleep(1100);

        //then
        tokenProvider.login(code);
    }
}