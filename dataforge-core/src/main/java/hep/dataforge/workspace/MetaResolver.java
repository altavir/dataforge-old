/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.workspace;

import hep.dataforge.meta.Meta;

/**
 * An interface to resolve configuration for given task and target
 * @author Alexander Nozik <altavir@gmail.com>
 */
public interface MetaResolver {
    Meta resolve(String taskName, String target);
}
