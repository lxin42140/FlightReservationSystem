package util.helper;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.NumberFormat;

public class BigDecimalHelper {

    private static final int PRECISION = 19;
    private static final int SCALE = 4;
    private static final int ROUNDING_MODE_INT = BigDecimal.ROUND_HALF_UP;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public BigDecimalHelper() {
    }

    public static BigDecimal createBigDecimal(String val) {
        BigDecimal bigDecimal = new BigDecimal(val, new MathContext(PRECISION, ROUNDING_MODE));
        bigDecimal.setScale(SCALE);

        return bigDecimal;
    }

    public static String formatCurrency(BigDecimal bigDecimal) {
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance();

        return numberFormat.format(bigDecimal);
    }

    public static int getPRECISION() {
        return PRECISION;
    }

    public static int getSCALE() {
        return SCALE;
    }

    public static int getROUNDING_MODE_INT() {
        return ROUNDING_MODE_INT;
    }

    public static RoundingMode getROUNDING_MODE() {
        return ROUNDING_MODE;
    }
}
