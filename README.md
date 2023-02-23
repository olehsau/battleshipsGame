Aplikacja łączy się z inną aplikacją i rozgrywa partię gry w okręty.

### Parametry uruchomieniowe
Aplikacja obługuje następujące parametry:
* `-mode [server|client]` - wskazuje tryb działania (jako serwer: przyjmuje połączenie, jako klient: nawiązuje połączenie z serwerem)
* `-host N` - tylko w trybie klienta: IP serwera, z którym aplikacja będzie się komunikować. Przykład: `-host 192.168.2.12`
* `-port N` - port, na którym aplikacja ma się komunikować. Przykład: `-port 4567`
* `-map map-file` - ścieżka do pliku zawierającego mapę z rozmieszczeniem statków (format opisany w sekcji Mapa).

Komunikacja odbywa się z użyciem protokołu TCP, z kodowaniem UTF-8.
Klient i serwer wysyłają sobie na przemian _wiadomość_, która składa się z 2 części: _komendy_ i _współrzędnych_, odzielonych znakiem `;`, i zakończonych znakiem końca linii `\n`.
Format wiadomości: `komenda;współrzędne\n`
