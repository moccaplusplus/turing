@ECHO OFF
java --module-path %~dp0\machine.jar --module turing.machine/turing.machine.Cmd %*
