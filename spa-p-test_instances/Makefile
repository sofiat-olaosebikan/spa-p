SHELL := /bin/bash
INSTANCES := $(shell cut -d' ' -f1 all.txt)
RESULTS := results
PYTHON := python3

dir-%:
	mkdir -p $(RESULTS)


results/$()%:
	mkdir -p $(RESULTS)
	$(PYTHON) ../spa-p-codes/ip-spa-p.py $*  | tail -n1 > $(RESULTS)/$*

run:  $(foreach i, $(INSTANCES), $(RESULTS)/$(i))
