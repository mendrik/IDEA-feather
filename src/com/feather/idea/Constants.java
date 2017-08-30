package com.feather.idea;

import java.util.regex.Pattern;

public interface Constants {
    Pattern pattern = Pattern.compile("\\{\\{([^{}]+?)}}");
}
