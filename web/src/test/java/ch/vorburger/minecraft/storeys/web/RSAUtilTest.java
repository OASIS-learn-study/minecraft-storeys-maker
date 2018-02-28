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
package ch.vorburger.minecraft.storeys.web;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RSAUtilTest {

    @Test
    public void shouldCryptAndDecrypt() {
        //given
        String secret = "This is secret";
        RSAUtil util = new RSAUtil();

        //when
        String encrypted = util.encrypt(secret, util.keyPair.getPublic());

        //then
        assertEquals(secret, util.decrypt(encrypted));
    }

    @Test
    public void shouldEncryptWithOtherPublicKey() {
        //given
        String secret = "This is secret";
        String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmMXIzlnMiLNMcDcbp29dl1tk1utkUG/4N6LOlTXoCSiuxt14zHpsl" +
                "k79+kkV+Gk1A7VeV2jY2sUaM0/aJCvdZrXiP635440GKs87xuRNqV5rIBpQA6HiCXh5gkVUgXO3PzTzxRA+2CaWDcSKnLHRM+3750TltybLq" +
                "JjPTs2Dlpp5zjV8IR/670OeeKA7vvMJArzSDsnfYGHEAACT6i+7qp6XBZxVPRnbgKR1c4CDrTK9OJ5ktf9fpDgrGa66AMCha+H8YR7WHeRLo" +
                "d1LaWjYmNwhJ+eC8dNxNRVDTcSgdXCuX8aShYQr3oltttuHk21mfYApmmC0Eeda/9aa29SbeQIDAQAB";
        String privateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCYxcjOWcyIs0xwNxunb12XW2TW62RQb/g3os6VNegJKK7G" +
                "3XjMemyWTv36SRX4aTUDtV5XaNjaxRozT9okK91mteI/rfnjjQYqzzvG5E2pXmsgGlADoeIJeHmCRVSBc7c/NPPFED7YJpYNxIqcsdEz7fvn" +
                "ROW3JsuomM9OzYOWmnnONXwhH/rvQ554oDu+8wkCvNIOyd9gYcQAAJPqL7uqnpcFnFU9GduApHVzgIOtMr04nmS1/1+kOCsZrroAwKFr4fxh" +
                "HtYd5Euh3UtpaNiY3CEn54Lx03E1FUNNxKB1cK5fxpKFhCveiW2224eTbWZ9gCmaYLQR51r/1prb1Jt5AgMBAAECggEBAICrTEYnsbGMuZtl" +
                "fqAVltDsw9CTrwhkVb2eHLnlDgnwvst1KtGlZqFt5FERjzSKf4EhF1UtVD9ldHy1C3gdjbp9BBI4GFhDpWcW5TTNgm9cu4LtRmjYxkTNuCE0" +
                "/UiOSk8s7QcTeKqaTRVJbKkuxpEjJl+RUpxgS3UzRp8LToHfR0OznagKhBpz9HjhS60G5JLhhoGh7doVk7bRHNuVEUHN+jOqSjw6GnKFpbI7" +
                "jNEDfGURx695Z9z4qyjUFrkV72/rdXoNHnX/Atozpoghs/+R9Y9j1vOkDcgC7LCpDMxaonXrlomc5U7Mjb6iQQqQxMeaO+Lq7HC0VmCvMAWv" +
                "ohECgYEA4PlvkquSnRdfpayM23YNv74bFSyG8Ap4wHDyHbOh2Ewl9y32d6ja14ritfnVJKYptdL+eno10cmoki/E3Vy9rc6vymWdjzv4g+md" +
                "Xm9Dh94yQr3KUShW3vn7zqRVnhYhcespz6zNaYwCIjPFFigvu0x1FPlfVxftCCJarxZvWl0CgYEArddQs1Aa0Eei+LlGSxxwPGO86hDbVJwD" +
                "5XPA99JR0FJWVzuIZYYW8MsLsIOMDQ8FOCE6bvmCxebD99+DnbeOr3vFXnLWFEEZNswX1P2h5O7b6ogkjtS/WwG2BRIJ7ZLtfhkm7qtgR9FC" +
                "utlzXGQv/pbTyh19A3KR28bgS+bwS80CgYAo/2vEt72NISBOCcbVi4rv4U4AKtAgWFgvAaYtoXmh+CaHB/o9TikrVGUCMpgUCK/bMassWK8M" +
                "Hwq4MTpX+LmcPektIh13Z79fEqiZ9/6/eM+B+CIxbf9+vOGVg78SgwfvQaKp9NyuGlSdNP/6jLI+GK3Rd6xXK8YHUHNkKtt6BQKBgEpEOCfC" +
                "yxG76PXVQSCJj7Fc8rIUbX8h/9AguYmmAmm7I0O1HToyJM7OD7pjGIvjpNkr6OjME4A0a/gTNwlEB+MUP2dwK+EPRpRQcMX/bRB+stjpPKUA" +
                "YpegjYBxCmjCYk+Cs003TbSgw59Lkqsf6hkbmnM4UdYNNh7sQZprmLDpAoGAHjuCeGuTifzWlducZa97hJpEroRhHT4vTZUbX/WhPN10brdo" +
                "tm2JmlD8ENJx5pcbSMAT+j/8FCxeqP/D5bKy7t/MloWZBZC1yTFCn0AlaMonegStsG1B4bxKE26i5RbP476BZ+MlbqX+20IJxtRmerubY/34" +
                "rTKoHU0zKDBUct0=";

        RSAUtil util = new RSAUtil();

        //when
        String encrypted = util.encrypt(secret, publicKey);

        //then
        assertEquals(secret, util.decrypt(encrypted, privateKey));

    }
}
