@ECHO OFF
java --module-path %~dp0\turing-machine.jar --module turing.machine/turing.cmd.Cmd %*
