SBT		?= sbt
SBT_FLAGS	?= -Dsbt.log.noformat=true
RM_DIRS 	:= test-outputs test-reports
CLEAN_DIRS	:= doc

.PHONY:	smoke publish-local clean jenkins-build

default:	publish-local

smoke:
	$(SBT) $(SBT_FLAGS) compile

publish-local:
	$(SBT) $(SBT_FLAGS) publish-local

clean:
	$(SBT) $(SBT_FLAGS) clean
	for dir in $(CLEAN_DIRS); do $(MAKE) -C $$dir clean; done
	$(RM) -r $(RM_DIRS)

jenkins-build:
	$(SBT) $(SBT_FLAGS) clean scalastyle scct:test publish-local

test:
	$(SBT) $(SBT_FLAGS) scct:test
