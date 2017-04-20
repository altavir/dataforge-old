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
package hep.dataforge.stat.fit;

import hep.dataforge.description.NodeDef;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.maths.NamedVector;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaUtils;
import hep.dataforge.names.Names;
import hep.dataforge.values.NamedValueSet;
import hep.dataforge.values.Value;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;

/**
 * Реализация набора параметров, которая будет потом использоваться в Result,
 * Fitter и Spectrum
 * <p>
 * Подразумевается, что ParamSet обязательно содержит помимо значения хотя бы
 * ошибку.
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class ParamSet implements NamedValueSet, Serializable {

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

    public ParamSet(ParamSet other) {
        this.params = new LinkedHashMap<>();
        for (Param par : other.getParams()) {
            params.put(par.name(), par.copy());
        }
    }

    public ParamSet() {
        this.params = new LinkedHashMap<>();
    }

    public ParamSet(NamedValueSet values) {
        this.params = new LinkedHashMap<>(values.names().size());
        for (String name : values.names()) {
            this.params.put(name, new Param(name, values.getDouble(name)));
        }
    }

    //    @NodeDef(name = "param", multiple = true, info = "The fit prameter", target = "method::hep.dataforge.stat.fit.Param.fromMeta")
    @NodeDef(name = "params", info = "Used as a wrapper for 'param' elements.")
    public static ParamSet fromMeta(Meta cfg) {

        Meta params;
        if (cfg.hasMeta("params")) {
            params = cfg.getMeta("params");
        } else if ("params".equals(cfg.getName())) {
            params = cfg;
        } else {
            return new ParamSet();
        }


        ParamSet set = new ParamSet();
        MetaUtils.nodeStream(params).forEach(entry -> {
            if (entry.getKey() != "params") {
                set.setPar(Param.fromMeta(entry.getValue()));
            }
        });

        return set;
    }

    /**
     * Read parameter set from lines using 'name'	= value ± error	(lower,upper)
     * syntax
     *
     * @param str a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.stat.fit.ParamSet} object.
     */
    public static ParamSet fromString(String str) {
        Scanner scan = new Scanner(str);
        ParamSet set = new ParamSet();
        while (scan.hasNextLine()) {
            set.setPar(Param.fromString(scan.nextLine()));
        }
        return set;
    }

    @Override
    public Optional<Value> optValue(String path) {
        return optByName(path).map(par -> Value.of(par.value()));
    }


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

    public Optional<Param> optByName(String str) {
        return Optional.ofNullable(this.params.get(str));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        assert params != null;
        return params.size();
    }

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
     * @return a {@link hep.dataforge.maths.NamedVector} object.
     * @throws hep.dataforge.exceptions.NameNotFoundException if any.
     */
    public NamedVector getParErrors(String... names) throws NameNotFoundException {
        if (names.length == 0) {
            names = this.namesAsArray();
        }
        assert this.names().contains(names);

        double[] res = new double[names.length];

        for (int i = 0; i < res.length; i++) {
            res[i] = this.getError(names[i]);

        }
        return new NamedVector(names, res);
    }

    /**
     * <p>
     * getParValues.</p>
     *
     * @param names a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.maths.NamedVector} object.
     * @throws hep.dataforge.exceptions.NameNotFoundException if any.
     */
    public NamedVector getParValues(String... names) throws NameNotFoundException {
        if (names.length == 0) {
            names = this.namesAsArray();
        }
        assert this.names().contains(names);

        double[] res = new double[names.length];

        for (int i = 0; i < res.length; i++) {
            res[i] = this.getDouble(names[i]);

        }
        return new NamedVector(names, res);
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
     * @return a {@link hep.dataforge.stat.fit.ParamSet} object.
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
     * <p>
     * Метод возвращает значение параметра с именем str
     *
     * @param str
     */
    @Override
    public Double getDouble(String str) throws NameNotFoundException {
        Param p;
        p = this.getByName(str);
        return p.value();
    }

    /**
     * {@inheritDoc}
     */
    public double[] getArray(String... names) {
        return this.getParValues(names).getArray();
    }

    /**
     * Searches set for a parameter with the same name and replaces it. Only
     * link is replaced, use {@code copy} to make a deep copy.
     * <p>
     * In case name not found adds a new parameter
     *
     * @param input a {@link hep.dataforge.stat.fit.Param} object.
     * @return a {@link hep.dataforge.stat.fit.ParamSet} object.
     */
    public ParamSet setPar(Param input) {
        this.params.put(input.name(), input);
        return this;
    }


    private ParamSet upadatePar(String name, Consumer<Param> consumer){
        Param par;
        if (!params.containsKey(name)) {
            LoggerFactory.getLogger(getClass())
                    .trace("Parameter with name '{}' not found. Adding a new parameter with this name.", name);
            par = new Param(name);
            this.params.put(name, par);
        } else {
            par = getByName(name);
        }
        consumer.accept(par);
        return this;
    }

    public ParamSet setPar(String name, double value, double error) {
        return upadatePar(name, (par)-> {
            par.setValue(value);
            par.setErr(error);
        });
    }

    public ParamSet setPar(String name, double value, double error, Double lower, Double upper) {
        return upadatePar(name, (par)-> {
            par.setValue(value);
            par.setErr(error);
            par.setDomain(lower, upper);
        });
    }

    public ParamSet setParValue(String name, double value) {
        return upadatePar(name, (par)-> {
            par.setValue(value);
        });
    }

    public ParamSet setParDomain(String name, Double lower, Double upper) throws NameNotFoundException {
        Param Par;
        Par = getByName(name);

        Par.setDomain(lower, upper);
        return this;
    }

    public ParamSet setParError(String name, double value) throws NameNotFoundException {
        Param Par;
        Par = getByName(name);
        Par.setErr(value);
        return this;
    }

    /**
     * method to set all parameter errors.
     *
     * @param errors
     * @return a {@link hep.dataforge.stat.fit.ParamSet} object.
     * @throws hep.dataforge.exceptions.NameNotFoundException if any.
     */
    public ParamSet setParErrors(NamedValueSet errors) throws NameNotFoundException {
        if (!this.names().contains(errors.names())) {
            throw new NameNotFoundException();
        }
        for (String name : errors.names()) {
            this.setParError(name, errors.getDouble(name));
        }
        return this;
    }

    /**
     * method to set all parameter values.
     *
     * @param values
     * @return a {@link hep.dataforge.stat.fit.ParamSet} object.
     * @throws hep.dataforge.exceptions.NameNotFoundException if any.
     */
    public ParamSet setParValues(NamedValueSet values) throws NameNotFoundException {
        if (!this.names().contains(values.names())) {
            throw new NameNotFoundException();
        }
        int i;
        for (String name : values.names()) {
            this.setParValue(name, values.getDouble(name));
        }
        return this;
    }

    /**
     * <p>
     * updateFrom.</p>
     *
     * @param set a {@link hep.dataforge.stat.fit.ParamSet} object.
     */
    public void updateFrom(ParamSet set) {
        set.getParams().stream().forEach((p) -> {
            setPar(p);
        });
    }

}
