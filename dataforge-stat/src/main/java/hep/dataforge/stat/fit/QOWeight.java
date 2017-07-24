package hep.dataforge.stat.fit;

import hep.dataforge.names.AbstractNamedSet;
import hep.dataforge.values.Values;

/**
 * Created by darksnake on 17-Oct-16.
 */
class QOWeight extends AbstractNamedSet {

    /**
     * Производная спектра по параметру в тета-0 Первый индекс - номер
     * переменной, второй - номер точки из data;
     */
    private double[][] derivs;
    /**
     * КВАДРАТ! ошибки в i-той точке Пока используем экспериментальную ошибку, а
     * там подумаем
     */
    private double[] dispersion;
    private FitState source;
    private Values theta; // точка, в которой веса вычислены

    QOWeight(FitState source, String[] list) {
        super(list);
        this.source = source;
    }

    QOWeight(FitState source, String[] fitPars, ParamSet theta) {
        this(source, fitPars);
        this.update(theta);
    }

    /**
     * Производные от значения спектра в точке по параметрам. Первый индекс -
     * номер точки, второй - номер параметра.
     *
     * @return the derivs
     */
    public double[][] getDerivs() {
        return derivs;
    }

    /**
     * Квадрат ошибки точки.
     *
     * @return the dispersion
     */
    public double[] getDispersion() {
        return dispersion;
    }

    /**
     * Состояние,для которого посчитан вес.
     *
     * @return the source
     */
    public FitState getSource() {
        return source;
    }

    /**
     * Набор параметров, в котором посчитан вес.
     *
     * @return the theta
     */
    public Values getTheta() {
        if (this.theta == null) {
            throw new IllegalStateException("Update operation for weight is required.");
        }
        return theta;
    }

    /**
     * Обновление весов. На всякий случай требуем явной передачи набора
     * параметров
     *
     * @param set
     */
    final void update(ParamSet set) {

        if (getSource().getDataSize() <= 0) {
            throw new IllegalStateException("Data is not set.");
        }
        theta = set.copy();

        int i;
        int k;
        dispersion = new double[getSource().getDataSize()];
        derivs = new double[getNames().size()][getSource().getDataSize()];

        for (i = 0; i < getSource().getDataSize(); i++) {

            this.dispersion[i] = getSource().getDispersion(i, set);
            for (k = 0; k < this.getNames().size(); k++) {
                derivs[k][i] = getSource().getDisDeriv(this.getNames().get(k), i, set);
            }
        }

    }
}
