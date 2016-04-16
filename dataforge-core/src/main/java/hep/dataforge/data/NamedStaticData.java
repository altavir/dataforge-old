/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data;


public class NamedStaticData<T> extends StaticData<T> implements NamedData<T> {
    
    private final String name;

    public NamedStaticData(String name, T object) {
        super(object);
        this.name = name;
    }

    public NamedStaticData(String name, T object, Class<T> type) {
        super(object, type);
        this.name = name;
    }


    @Override
    public String getName() {
        return name;
    }
    
}
