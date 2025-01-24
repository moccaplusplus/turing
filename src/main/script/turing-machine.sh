#!/usr/bin/env bash
java --module-path $(dirname $0)/turing-machine.jar --module turing.machine/turing.machine.Main $@