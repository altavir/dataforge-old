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

import hep.dataforge.description.ValueDef;
import hep.dataforge.meta.Meta;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The main parameter implementation
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class Param {

    private double err = Double.NaN;
    private double lowerBound = Double.NEGATIVE_INFINITY; // Область, в которой параметр может существовать.
    private final String name; //Название параметра
    private double upperBound = Double.POSITIVE_INFINITY;
    private double value = Double.NaN; //значение параметра

    /**
     * Creates parameter with name.
     *
     * @param str a {@link java.lang.String} object.
     */
    public Param(String str) {
        name = str;
    }

    /**
     * <p>
     * Constructor for Param.</p>
     *
     * @param str a {@link java.lang.String} object.
     * @param value a double.
     */
    public Param(String str, double value) {
        this(str);
        this.value = value;
    }

    /**
     * <p>
     * fromAnnotation.</p>
     *
     * @param cfg a {@link hep.dataforge.meta.Meta} object.
     * @return a {@link hep.dataforge.datafitter.Param} object.
     */
    @ValueDef(name = "name", required = true, info = "Parameter name.")
    @ValueDef(name = "value", type = "NUMBER", required = true, info = "Parameter value.")
    @ValueDef(name = "err", type = "NUMBER", info = "Parameter error or in general case inversed square root of the weight.")
    @ValueDef(name = "lower", type = "NUMBER", info = "Parameter lower boundary.")
    @ValueDef(name = "upper", type = "NUMBER", info = "Parameter upper boundary.")
    public static Param fromAnnotation(Meta cfg) {
        String name = cfg.getValue("name").stringValue();
        Param res = new Param(name);
        res.setErr(cfg.getDouble("err", Double.NaN));
        res.setDomain(cfg.getDouble("lower", Double.NEGATIVE_INFINITY),
                cfg.getDouble("upper", Double.POSITIVE_INFINITY));
        res.setValue(cfg.getDouble("value", Double.NaN));
        return res;
    }

    /**
     * Read fir parameter from String using 'name'	= value ± error	(lower,upper)
     * syntax
     *
     * @param str a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.datafitter.Param} object.
     */
    public static Param fromString(String str) {
        Matcher matcher = Pattern.compile(
                "\\'(?<name>.*)\\'\\s*=*\\s*(?<value>[\\.\\deE]*)\\s*±\\s*(?<error>[\\.\\deE]*)(?:\\s*\\((?<lower>.*),\\s*(?<upper>.*)\\))?"
        ).matcher(str);
        if (matcher.matches()) {
            String name = matcher.group("name");
            double value = Double.valueOf(matcher.group("value"));
            double error = Double.valueOf(matcher.group("error"));
            Param par = new Param(name, value);
            par.setErr(error);
            if (matcher.group("lower") != null && matcher.group("upper") != null) {
                double lower = Double.valueOf(matcher.group("lower"));
                double upper = Double.valueOf(matcher.group("upper"));
                par.setDomain(lower, upper);
            }
            return par;
        } else {
            throw new IllegalArgumentException();
        }

    }

    /**
     * <p>
     * copy.</p>
     *
     * @return a {@link hep.dataforge.datafitter.Param} object.
     */
    public Param copy() {
        Param res = new Param(this.name);
        res.value = this.value;
        res.err = this.err;
        res.lowerBound = this.getLowerBound();
        res.upperBound = this.getUpperBound();
        return res;
    }

    /**
     * <p>
     * Getter for the field <code>err</code>.</p>
     *
     * @return a double.
     */
    public double getErr() {
        return err;
    }

    /**
     * <p>
     * Getter for the field <code>lowerBound</code>.</p>
     *
     * @return the lowerDomain
     */
    public Double getLowerBound() {
        return lowerBound;
    }

    /**
     * <p>
     * Getter for the field <code>upperBound</code>.</p>
     *
     * @return the upperDomain
     */
    public Double getUpperBound() {
        return upperBound;
    }

    /**
     * <p>
     * name.</p>
     *
     * @return name of the parameter.
     */
    public String name() {
        return name;
    }

    /**
     * <p>
     * setDomain.</p>
     *
     * @param lower a double.
     * @param upper a double.
     */
    public void setDomain(double lower, double upper) {
        /*Метод определяет область параметра, попутно проверяя, что она задана
         правильно*/
        upperBound = upper;
        lowerBound = lower;

        if (getUpperBound() <= getLowerBound()) {
            throw new RuntimeException("Wrong domain.");
        }
    }

    /**
     * <p>
     * Setter for the field <code>err</code>.</p>
     *
     * @param error a double.
     */
    public void setErr(double error) {
        /*стандартная ошибка или любая другая величина, несущая этот смысл*/
//        if(error<0) throw new CoreException("Error for parameter must be positive.");
        assert error >= 0 : "Error for parameter must be positive.";
        err = error;
    }

    /**
     * Автоматически учитывает границы параметра
     *
     * @param value the value to set
     */
    public void setValue(double value) {
        if (value < this.lowerBound) {
            this.value = lowerBound;
        } else if (value > this.upperBound) {
            this.value = upperBound;
        } else {
            this.value = value;
        }
    }

    /**
     * <p>
     * value.</p>
     *
     * @return the value
     */
    public double value() {
        return value;
    }

    /**
     * <p>
     * isConstrained.</p>
     *
     * @return a boolean.
     */
    public boolean isConstrained() {
        return this.lowerBound > Double.NEGATIVE_INFINITY || this.upperBound < Double.POSITIVE_INFINITY;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public String toString() {
        if (Double.isNaN(this.err)) {
            return String.format("'%s'\t= %g", this.name, this.value);
        } else {
            int digits;
            if (value == 0) {
                digits = (int) Math.max(-Math.log10(err) + 1, 1);
            } else {
                digits = (int) (Math.log10(Math.abs(value)));
                digits = digits - (int) (Math.log10(err)) + 3;
            }

            if (isConstrained()) {
                return String.format("'%s'\t= %." + digits + "g \u00b1 %.2g\t(%g,%g)", this.name, this.value, this.err, this.lowerBound, this.upperBound);
            } else {
                return String.format("'%s'\t= %." + digits + "g \u00b1 %.2g", this.name, this.value, this.err);
            }
        }
    }

}
