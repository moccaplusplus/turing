# Turing Machine Emulator

## Wymagania

- Java - JDK 21 lub wyższa.
- Należy pamiętać o ustawieniu zmiennej środowiskowej `JAVA_HOME` wskazującej
      na katalog gdzie zainstalowaliśmy JDK.

## Budowanie Projektu

Abu zbudować projekt, w katalogu głównym projektu
wpisujemy w konsoli:

```shell
./mvnw package
```
Zbudowany projekt znajduje się w katalogu `target`.

Aby zbudować projekt bez uruchamiania testów wystarczy dodać opcję `-DskipTests`.
```shell
./mvnw package -DskipTests
```

## Uruchomienie Projektu

### Uruchomienie w trybie help

Aby wyświetlić pomoc, zbudowany projekt uruchamiamy, 
wpisując w konsoli, w katalogu głównym projektu:
```shell
./target/turing-machine -h
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
w katalogu `src/test/resources` wewnątrz projektu.

Aby uruchomić zbudowany projekt z przykładowym plikiem wejściowym,
w katalogu głównym projektu, wpisujemy w konsoli:
```shell
./target/turing-machine ./src/test/resources/input.txt
```
Wynik działania programu znajduje się w pliku `out.log` w katalogu, w którym 
uruchomiliśmy program (o ile nie podaliśmy innej ścieżki w opcjach uruchomienia programu)