/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.work;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

public class DefaultGoal implements Goal {

    private Map<String, Binding> bindings = new HashMap<>();

    @Override
    public void bind(Goal dependency, String source, String target) {
        if(!bindings.containsKey(target)){
            throw new RuntimeException("Slot for this binding does noe exist");
        } else {
            bindings.get(target).
        }
    }

    public Future<Map<String, Object>> run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    protected class Binding<T> {

        /**
         * The name of output slot of bound goal
         */
        String source;

        /**
         * The bound goal
         */
        Goal dependency;

        boolean isBound() {
            return dependency != null;
        }
        
        void bind(Goal depenndency, String sourceSlot){
            this.dependency = depenndency;
            this.source = source;
        }

        T compute() throws Exception {
            return (T) dependency.run().get().get(source);
        }
        
        Class<T> getType(){
            
        }
        
        boolean multipleAllowed(){
            
        }
        
    }

}
