package io.jonasg.mother.csv;

import org.jspecify.annotations.Nullable;

public record Row(String[]headers,String[]values){

public @Nullable String column(String headerName){for(int i=0;i<headers.length;i++){if(headers[i].equals(headerName)){return values[i];}}return null;}}
