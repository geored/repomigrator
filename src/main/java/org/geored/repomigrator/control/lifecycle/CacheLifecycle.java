/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geored.repomigrator.control.lifecycle;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;

/**
 *
 * @author gorgigeorgievski
 */


@ApplicationScoped
public enum CacheLifecycle {
	
	LOADING,LOADED,PROCESSED
	
}
