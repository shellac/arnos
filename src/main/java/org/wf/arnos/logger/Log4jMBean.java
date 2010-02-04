/*
 * Copyright (c) 2009, University of Bristol
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1) Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3) Neither the name of the University of Bristol nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.wf.arnos.logger;

import java.util.ArrayList;
import java.util.Enumeration;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
@ManagedResource
public class Log4jMBean {

    @ManagedOperation
    public void activateInfo(String category) {
        LogManager.getLogger(category).setLevel(Level.INFO);
    }

    @ManagedOperation
    public void activateDebug(String category) {
        LogManager.getLogger(category).setLevel(Level.DEBUG);
    }

    @ManagedOperation
    public void activateWarn(String category) {
        LogManager.getLogger(category).setLevel(Level.WARN);
    }

    @ManagedOperation
    public void activateError(String category) {
        LogManager.getLogger(category).setLevel(Level.ERROR);
    }

    @ManagedOperation
    public void activateFatal(String category) {
        LogManager.getLogger(category).setLevel(Level.FATAL);
    }

    @ManagedOperation
    public ArrayList<String> listAllLoggers() {
        Enumeration loggers = LogManager.getCurrentLoggers();
        ArrayList<String> loggerArray = new ArrayList<String>();
        while (loggers.hasMoreElements())
        {
            org.apache.log4j.Logger l = (org.apache.log4j.Logger)loggers.nextElement();
            loggerArray.add(l.getName() + " ["+l.getLevel()+"]");
        }
        return loggerArray;
    }
}
