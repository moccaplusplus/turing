# Turing Machine Emulator

## Wymagania

- Java - JDK 21 lub wyższa.
- Należy pamiętać o ustawieniu zmiennej środowiskowej `JAVA_HOME` wskazującej
      na katalog gdzie zainstalowaliśmy JDK.

## Krótki Opis

Projekt składa się z dwóch podprojektów:

- machine - zawiera faktyczną implementację maszyny i prosty interfejs commandline.
- gui - dodaje interfejs uzytkownika GUI. 

Które znajdują się w podkatalogach o nazwach `machine` i `gui`.


## Budowanie Projektu

Abu zbudować projekt (oba podrpojekty), w katalogu głównym projektu
wpisujemy w konsoli:

```shell
./mvnw install
```
UWAGA:
Pierwsza instalacja będzie ściągać zależności z internetu. 
Zależności są tylko do podprojektu `gui`. Podprojekt `machine` 
nie korzysta z żadnych zewnętrznych bibliotek.

Zbudowane podprojekty znajdują się odpowiednio w katalogach:
- `gui/target`.
- `machine/target`.

Aby zbudować projekt bez uruchamiania testów wystarczy dodać opcję `-DskipTests`.
```shell
./mvnw install -DskipTests
```
## Uruchomienie Projektu

### Uruchomienie w trybie help

Aby wyświetlić pomoc, zbudowany projekt uruchamiamy, 
wpisując w konsoli, w katalogu głównym projektu:
```shell
./machine/target/turing-machine -h
```

Powinniśmy zobaczyć następujący output:
```
Usage: turing-machine [-h] [-c <charset>] path/to/input.file
    path/to/input.file     Path to input file with settings.
Options:
    -c, --charset          Optional. Input file encoding. Default: UTF-8.
    -o, --out              Optional. Path to output file. Default: ./out.log.
    -h, --help             Prints help.
```

### Uruchomienie ze scieżką do pliku

Przykładowy plik wejściowy `input.txt` znajduje się 
w katalogu `machine/src/test/resources` wewnątrz projektu.

Aby uruchomić zbudowany projekt z przykładowym plikiem wejściowym,
w katalogu głównym projektu, wpisujemy w konsoli:
```shell
./machine/target/turing-machine ./machine/src/test/resources/przykladowy_input.txt
```
Wynik działania programu znajduje się w pliku `out.log` w katalogu, w którym 
uruchomiliśmy program (o ile nie podaliśmy innej ścieżki w opcjach uruchomienia programu)

### Uruchomienie w trybie GUI

Aby uruchomić projekt wtrybie GUI, wpisujemy w konsoli, w katalogu głównym projektu:

```shell
./gui/target/turing-machine-gui
```

Lub robimy duble-click myszką na pliku `./gui/target/turing-machine-gui.cmd`.
