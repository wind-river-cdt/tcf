# *****************************************************************************
# * Copyright (c) 2012 Wind River Systems, Inc. and others.
# * All rights reserved. This program and the accompanying materials
# * are made available under the terms of the Eclipse Public License v1.0
# * which accompanies this distribution, and is available at
# * http://www.eclipse.org/legal/epl-v10.html
# *
# * Contributors:
# *     Wind River Systems - initial API and implementation
# *****************************************************************************

"""
TCF ContextQuery service interface.
"""

from tcf import services

# Service name.
NAME = "ContextQuery"


class ContextQueryService(services.Service):
    def getName(self):
        return NAME

    def query(self, querystr, done):
        """
        @param querystr - context query to be executed.
        @param done - command result call back object.
        @return - pending command handle.
        @see DoneQuery
        """
        raise NotImplementedError("Abstract method")

    def getAttrNames(self, done):
        """
        @param done - command result call back object.
        @return - pending command handle.
        @see DoneGetAttrNames
        """
        raise NotImplementedError("Abstract method")


class DoneQuery(object):
    "Call back interface for 'query' command."
    def doneQuery(self, token, error, ctxList):
        """
        Called when 'query' command is done.
        @param token - command handle.
        @param error - error object or None.
        @param ctxList - IDs of contexts matching the query.
        """
        pass


class DoneGetAttrNames(object):
    "Call back interface for 'getAttrNames' command."
    def doneGetAttrNames(self, token, error, attrNameList):
        """
        Called when 'getAttrNames' command is done.
        @param token - command handle.
        @param error - error object or None.
        @param attrNameList - List of the attributes supported by the agent.
        """
        pass
