package com.feather.idea;

import java.util.regex.Pattern;

public interface Constants {

    Pattern doubleBraces = Pattern.compile("\\{\\{([^{}]+?)}}");
    Pattern singleBraces = Pattern.compile("^[\"']?\\{(.+?)}[\"'/]?$");
    Pattern classSplitter = Pattern.compile("([^\\s\"]+)");
}
