#
# ch.vorburger.minecraft.storeys
#
# Copyright (C) 2016 - 2018 Michael Vorburger.ch <mike@vorburger.ch>
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as published
# by the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

keytool -genseckey -keystore keystore.jceks -storetype jceks -storepass '_2y47[-53YLf}/frv.Q"' -keyalg HMacSHA256 -keysize 2048 -alias HS256 -keypass '_2y47[-53YLf}/frv.Q"'
keytool -genseckey -keystore keystore.jceks -storetype jceks -storepass '_2y47[-53YLf}/frv.Q"' -keyalg HMacSHA384 -keysize 2048 -alias HS384 -keypass '_2y47[-53YLf}/frv.Q"'
keytool -genseckey -keystore keystore.jceks -storetype jceks -storepass '_2y47[-53YLf}/frv.Q"' -keyalg HMacSHA512 -keysize 2048 -alias HS512 -keypass '_2y47[-53YLf}/frv.Q"'
keytool -genkey -keystore keystore.jceks -storetype jceks -storepass '_2y47[-53YLf}/frv.Q"' -keyalg RSA -keysize 2048 -alias RS256 -keypass '_2y47[-53YLf}/frv.Q"' -sigalg SHA256withRSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360
keytool -genkey -keystore keystore.jceks -storetype jceks -storepass '_2y47[-53YLf}/frv.Q"' -keyalg RSA -keysize 2048 -alias RS384 -keypass '_2y47[-53YLf}/frv.Q"' -sigalg SHA384withRSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360
keytool -genkey -keystore keystore.jceks -storetype jceks -storepass '_2y47[-53YLf}/frv.Q"' -keyalg RSA -keysize 2048 -alias RS512 -keypass '_2y47[-53YLf}/frv.Q"' -sigalg SHA512withRSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360
keytool -genkeypair -keystore keystore.jceks -storetype jceks -storepass '_2y47[-53YLf}/frv.Q"' -keyalg EC -keysize 256 -alias ES256 -keypass '_2y47[-53YLf}/frv.Q"' -sigalg SHA256withECDSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360
keytool -genkeypair -keystore keystore.jceks -storetype jceks -storepass '_2y47[-53YLf}/frv.Q"' -keyalg EC -keysize 384 -alias ES384 -keypass '_2y47[-53YLf}/frv.Q"' -sigalg SHA384withECDSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360
keytool -genkeypair -keystore keystore.jceks -storetype jceks -storepass '_2y47[-53YLf}/frv.Q"' -keyalg EC -keysize 521 -alias ES512 -keypass '_2y47[-53YLf}/frv.Q"' -sigalg SHA512withECDSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360