/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.values;

/**
 *
 * @author Alexander Nozik <altavir@gmail.com>
 */
public class ValueChooserFactory {
    
    private ValueChooserFactory() {
    }
    
    public static ValueChooserFactory getInstance() {
        return ValueChooserFactoryHolder.INSTANCE;
    }
    
    private static class ValueChooserFactoryHolder {

        private static final ValueChooserFactory INSTANCE = new ValueChooserFactory();
    }
    
//    public ValueChooser build(ValueDescriptor descriptor){
//        
//    }
}
