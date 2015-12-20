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
package hep.dataforge.datafitter;

import hep.dataforge.context.GlobalContext;
import hep.dataforge.description.NodeDef;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.io.log.Logable;
import hep.dataforge.maths.NamedDoubleArray;
import hep.dataforge.maths.NamedDoubleSet;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Names;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;
import org.slf4j.LoggerFactory;

/**
 * Реализация набора параметров, которая будет потом использоваться в Result,
 * Fitter и Spectrum
 *
 * Подразумевается, что ParamSet обязательно содержит помимо значения хотя бы
 * ошибку.
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class ParamSet implements NamedDoubleSet {

    /**
     * <p>
     * fromAnnotation.</p>
     *
     * @param cfg a {@link hep.dataforge.meta.Meta} object.
     * @return a {@link hep.dataforge.datafitter.ParamSet} object.
     */
    @NodeDef(name = "param", multiple = true, info = "The fit prameter", target = "method::hep.dataforge.datafitter.Param.fromAnnotation")
    @NodeDef(name = "params", info = "Could be used as a wrapper for 'param' elements. Used solely on purpose of xml readability.")
    public static ParamSet fromAnnotation(Meta cfg) {
        if (cfg.hasNode("params")) {
            cfg = cfg.getNode("params");
        }

        if (cfg.hasNode("param")) {
            List<? extends Meta> params;
            params = cfg.getNodes("param");
            ParamSet set = new ParamSet();
            for (Meta param : params) {
                set.setPar(Param.fromAnnotation(param));
            }
            return set;
        } else {
            //Возрвщвем пустой лист. Нужно для совместимости со значениями параметров по-умолчанию
            return new ParamSet();
        }
    }

    /**
     * Read parameter set from lines using 'name'	= value ± error	(lower,upper)
     * syntax
     *
     * @param str a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.datafitter.ParamSet} object.
     */
    public static ParamSet fromString(String str) {
        Scanner scan = new Scanner(str);
        ParamSet set = new ParamSet();
        while (scan.hasNextLine()) {
            set.setPar(Param.fromString(scan.nextLine()));
        }
        return set;
    }

    private final HashMap<String, Param> params;

    /**
     * Generates set of parameters with predefined names.
     *
     * @param names an array of {@link java.lang.String} objects.
     * @throws hep.dataforge.exceptions.NameNotFoundException if any.
     */
    public ParamSet(String[] names) throws NameNotFoundException {
        int num = names.length;
        this.params = new LinkedHashMap<>();
        int i, j;

        for (i = 0; i < num - 1; i++) { //Проверяем, нет ли совпадающих имен
            for (j = i + 1; j < num; j++) {
                if (names[i].equals(names[j])) {
                    throw new NameNotFoundException("ParamSet naming error: Names are not unique");
                }
            }
        }

        for (i = 0; i < num; i++) {
            this.params.put(names[i], new Param(names[i]));
        }
    }

    /**
     * <p>
     * Constructor for ParamSet.</p>
     *
     * @param other a {@link hep.dataforge.datafitter.ParamSet} object.
     */
    public ParamSet(ParamSet other) {
        this.params = new LinkedHashMap<>();
        for (Param par : other.getParams()) {
            params.put(par.name(), par.copy());
        }
    }

    /**
     * <p>
     * Constructor for ParamSet.</p>
     */
    public ParamSet() {
        this.params = new LinkedHashMap<>();
    }

    /**
     * <p>
     * Constructor for ParamSet.</p>
     *
     * @param values a {@link hep.dataforge.maths.NamedDoubleSet} object.
     */
    public ParamSet(NamedDoubleSet values) {
        this.params = new LinkedHashMap<>(values.names().getDimension());
        for (String name : values.names()) {
            this.params.put(name, new Param(name, values.getValue(name)));
        }
    }

    /**
     * <p>
     * copy.</p>
     *
     * @return a {@link hep.dataforge.datafitter.ParamSet} object.
     */
    public ParamSet copy() {
        return new ParamSet(this);
    }

    /**
     * Returns link to parameter with specific name. Возвращает параметр по его
     * имени.
     *
     * @param str a {@link java.lang.String} object.
     * @return null if name is not found.
     * @throws hep.dataforge.exceptions.NameNotFoundException if any.
     */
    public Param getByName(String str) throws NameNotFoundException {
        Param res = this.params.get(str);
        if (res != null) {
            return res;
        } else {
            throw new NameNotFoundException(str);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDimension() {
        assert params != null;
        return params.size();
    }

    /**
     * <p>
     * getError.</p>
     *
     * @param str a {@link java.lang.String} object.
     * @return a double.
     * @throws hep.dataforge.exceptions.NameNotFoundException if any.
     */
    public double getError(String str) throws NameNotFoundException {
        Param P;
        P = this.getByName(str);
        return P.getErr();
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Names names() {
        return Names.of(this.params.keySet());
    }

    /**
     * <p>
     * getParErrors.</p>
     *
     * @param names a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.maths.NamedDoubleArray} object.
     * @throws hep.dataforge.exceptions.NameNotFoundException if any.
     */
    public NamedDoubleArray getParErrors(String... names) throws NameNotFoundException {
        if (names.length == 0) {
            names = this.namesAsArray();
        }
        assert this.names().contains(names);

        double[] res = new double[names.length];

        for (int i = 0; i < res.length; i++) {
            res[i] = this.getError(names[i]);

        }
        return new NamedDoubleArray(names, res);
    }

    /**
     * <p>
     * getParValues.</p>
     *
     * @param names a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.maths.NamedDoubleArray} object.
     * @throws hep.dataforge.exceptions.NameNotFoundException if any.
     */
    public NamedDoubleArray getParValues(String... names) throws NameNotFoundException {
        if (names.length == 0) {
            names = this.namesAsArray();
        }
        assert this.names().contains(names);

        double[] res = new double[names.length];

        for (int i = 0; i < res.length; i++) {
            res[i] = this.getValue(names[i]);

        }
        return new NamedDoubleArray(names, res);
    }

    /**
     * <p>
     * Getter for the field <code>params</code>.</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<Param> getParams() {
        return params.values();
    }

    /**
     * Returns a parameter set witch consists only of names presented as
     * parameter (values are also copied).
     *
     * @param names a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.datafitter.ParamSet} object.
     * @throws hep.dataforge.exceptions.NameNotFoundException if any.
     */
    public ParamSet getSubSet(String... names) throws NameNotFoundException {
        if (names.length == 0) {
            return this.copy();
        }
        int i;
        ParamSet res = new ParamSet(names);
        for (i = 0; i < names.length; i++) {
            res.params.put(names[i], this.getByName(names[i]).copy());
        }
        return res;
    }

    /**
     * {@inheritDoc}
     *
     * Метод возвращает значение параметра с именем str
     * @param str
     */
    @Override
    public double getValue(String str) throws NameNotFoundException {
        Param P;
        P = this.getByName(str);
        return P.value();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double[] getValues(String... names) {
        return this.getParValues(names).getValues();
    }

    /**
     * Searches set for a parameter with the same name and replaces it. Only
     * link is replaced, use {@code copy} to make a deep copy.
     *
     * In case name not found adds a new parameter
     *
     * @param input a {@link hep.dataforge.datafitter.Param} object.
     * @return a {@link hep.dataforge.datafitter.ParamSet} object.
     */
    public ParamSet setPar(Param input) {
        this.params.put(input.name(), input);
        return this;
    }

    /**
     * <p>
     * setPar.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param value a double.
     * @param error a double.
     * @return a {@link hep.dataforge.datafitter.ParamSet} object.
     */
    public ParamSet setPar(String name, double value, double error) {
        Param par;
        if (!params.containsKey(name)) {
            LoggerFactory.getLogger(getClass())
                    .debug("Parameter with name '{}' not found. Adding a new parameter with this name.", name);
            par = new Param(name);
            this.params.put(name, par);
        } else {
            par = getByName(name);
        }

        par.setValue(value);
        par.setErr(error);

        return this;
    }

    /**
     * <p>
     * setPar.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param value a double.
     * @param error a double.
     * @param lower a {@link java.lang.Double} object.
     * @param upper a {@link java.lang.Double} object.
     * @return a {@link hep.dataforge.datafitter.ParamSet} object.
     */
    public ParamSet setPar(String name, double value, double error, Double lower, Double upper) {
        Param par;
        if (!params.containsKey(name)) {
            LoggerFactory.getLogger(getClass())
                    .debug("Parameter with name '{}' not found. Adding a new parameter with this name.", name);
            par = new Param(name);
            this.params.put(name, par);
        } else {
            par = getByName(name);
        }

        par.setValue(value);
        par.setErr(error);
        par.setDomain(lower, upper);

        return this;
    }

    /**
     * <p>
     * setParDomain.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param lower a {@link java.lang.Double} object.
     * @param upper a {@link java.lang.Double} object.
     * @return a {@link hep.dataforge.datafitter.ParamSet} object.
     * @throws hep.dataforge.exceptions.NameNotFoundException if any.
     */
    public ParamSet setParDomain(String name, Double lower, Double upper) throws NameNotFoundException {
        Param Par;
        Par = getByName(name);

        Par.setDomain(lower, upper);
        return this;
    }

    /**
     * <p>
     * setParError.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param value a double.
     * @return a {@link hep.dataforge.datafitter.ParamSet} object.
     * @throws hep.dataforge.exceptions.NameNotFoundException if any.
     */
    public ParamSet setParError(String name, double value) throws NameNotFoundException {
        Param Par;
        Par = getByName(name);
        Par.setErr(value);
        return this;
    }

    /**
     * method to set all parameter errors.
     *
     * @param errors a {@link hep.dataforge.maths.NamedDoubleSet} object.
     * @return a {@link hep.dataforge.datafitter.ParamSet} object.
     * @throws hep.dataforge.exceptions.NameNotFoundException if any.
     */
    public ParamSet setParErrors(NamedDoubleSet errors) throws NameNotFoundException {
        if (!this.names().contains(errors.names())) {
            throw new NameNotFoundException();
        }
        for (String name : errors.names()) {
            this.setParError(name, errors.getValue(name));
        }
        return this;
    }

    /**
     * <p>
     * setParValue.</p>
     *
     * @param name parameter name.
     * @param value a double.
     * @return a {@link hep.dataforge.datafitter.ParamSet} object.
     */
    public ParamSet setParValue(String name, double value) {
        Param par;
        if (!params.containsKey(name)) {
            LoggerFactory.getLogger(getClass())
                    .debug("Parameter with name '{}' not found. Adding a new parameter with this name.", name);
            par = new Param(name);
            this.params.put(name, par);
        } else {
            par = getByName(name);
        }

        par.setValue(value);
        return this;
    }

    /**
     * method to set all parameter values.
     *
     * @param values a {@link hep.dataforge.maths.NamedDoubleSet} object.
     * @return a {@link hep.dataforge.datafitter.ParamSet} object.
     * @throws hep.dataforge.exceptions.NameNotFoundException if any.
     */
    public ParamSet setParValues(NamedDoubleSet values) throws NameNotFoundException {
        if (!this.names().contains(values.names())) {
            throw new NameNotFoundException();
        }
        int i;
        for (String name : values.names()) {
            this.setParValue(name, values.getValue(name));
        }
        return this;
    }

    /**
     * <p>
     * updateFrom.</p>
     *
     * @param set a {@link hep.dataforge.datafitter.ParamSet} object.
     */
    public void updateFrom(ParamSet set) {
        for (Param p : set.getParams()) {
            setPar(p);
        }
    }

}