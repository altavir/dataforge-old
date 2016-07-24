/* 
 * Copyright 2015 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hep.dataforge.fitting;

import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.names.Named;
import java.util.Arrays;

/**
 * <p>
 * FitTask class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class FitTask implements Annotated, Named {

    /**
     * Constant <code>TASK_RUN="fit"</code>
     */
    public static final String TASK_RUN = "fit";
    /**
     * Constant <code>TASK_SINGLE="single"</code>
     */
    public static final String TASK_SINGLE = "single";
    /**
     * Constant <code>TASK_COVARIANCE="covariance"</code>
     */
    public static final String TASK_COVARIANCE = "covariance";

    private static final String NAME = "name";
    private static final String FREE_PARAMETERS = "freepars";
    private static final String ENGINE_NAME = "engine";
    private static final String METHOD_NAME = "method";
    private static final String DEFAULT_METHOD_NAME = "default";
//    private static final String MODEL_ANNOTATION_NAME = "model";    

//    private final String taskName;
//    private final Annotation taskDescription;
//    private String[] freePars;
    private final Meta taskDescription;

    /**
     * <p>
     * Constructor for FitTask.</p>
     *
     * @param taskAnnotation a {@link hep.dataforge.meta.Meta}
     * object.
     */
    public FitTask(Meta taskAnnotation) {
//        this.taskName = taskAnnotation.getString(NAME, TASK_RUN);
//        if (taskAnnotation.hasValue(FREE_PARAMETERS)) {
//            List<Value> values = taskAnnotation.getValues(FREE_PARAMETERS);
//            this.freePars = new String[values.size()];
//            for (int i = 0; i < values.size(); i++) {
//                freePars[i] = values.get(i).stringValue();
//            }
//        }

        this.taskDescription = taskAnnotation;
    }

    /**
     * <p>
     * Constructor for FitTask.</p>
     *
     * @param engineName a {@link java.lang.String} object.
     * @param taskName a {@link java.lang.String} object.
     * @param methodName
     * @param freePars an array of {@link java.lang.String} objects.
     */
    public FitTask(String engineName, String taskName, String methodName, String[] freePars) {
        taskDescription = new MetaBuilder("task")
                .putValue(NAME, taskName)
                .putValue(ENGINE_NAME, engineName)
                .putValue(METHOD_NAME, methodName)
                .putValues(FREE_PARAMETERS, freePars)
                .build();
    }

    public FitTask(String engineName, String taskName, String[] freePars) {
        this(engineName, taskName, DEFAULT_METHOD_NAME, freePars);
    }

    public FitTask(String engineName, String taskName) {
        taskDescription = new MetaBuilder("task")
                .putValue(NAME, taskName)
                .putValue(ENGINE_NAME, engineName)
                .build();
    }

    /**
     * <p>
     * Constructor for FitTask.</p>
     *
     * @param taskName a {@link java.lang.String} object.
     */
    public FitTask(String taskName) {
//        this.taskName = taskName;
        taskDescription = new MetaBuilder("task").putValue(NAME, taskName).build();
    }

    /**
     * <p>
     * getEngineName.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getEngineName() {
        return taskDescription.getString(ENGINE_NAME, QOWFitEngine.QOW_ENGINE_NAME);
    }

    /**
     * <p>
     * getMethodName.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMethodName() {
        return taskDescription.getString(METHOD_NAME, DEFAULT_METHOD_NAME);
    }

    /**
     * Если передается null или пустой массив, то считается что свободны все
     * параметры Данный метод не учитывает априорной информации. Параметр, по
     * которому задана априорная информация считается свободным в смысле фита.
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getFreePars() {
        if (meta().hasValue(FREE_PARAMETERS)) {
            return meta().getStringArray(FREE_PARAMETERS);
        } else {
            return new String[0];
        }
    }

    /**
     * Название задачи. Должно быть строго фиксированным, чтобы оно могло быть
     * распознано программой
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return meta().getString(NAME, "fit");
    }

    /**
     * <p>
     * Getter for the field <code>taskDescription</code>.</p>
     *
     * @return a {@link hep.dataforge.meta.Meta} object.
     */
    @Override
    public Meta meta() {
        return taskDescription;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public String toString() {
        String parameters;
        String[] freePars = getFreePars();
        
        if(freePars == null || freePars.length == 0){
            parameters = "all parameters";
        } else {
            parameters = Arrays.toString(freePars);
        }
            
        return getName() + "(" + parameters + ")";
    }

}
