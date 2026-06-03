package ru.mai.mathoptimization.dto;

public class VariantDto {

    private int id;
    private String title;
    private String method;
    private String methodLabel;
    private boolean supportsOneDimensional;
    private FunctionDefinitionDto f1;
    private FunctionDefinitionDto f2;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMethodLabel() {
        return methodLabel;
    }

    public void setMethodLabel(String methodLabel) {
        this.methodLabel = methodLabel;
    }

    public boolean isSupportsOneDimensional() {
        return supportsOneDimensional;
    }

    public void setSupportsOneDimensional(boolean supportsOneDimensional) {
        this.supportsOneDimensional = supportsOneDimensional;
    }

    public FunctionDefinitionDto getF1() {
        return f1;
    }

    public void setF1(FunctionDefinitionDto f1) {
        this.f1 = f1;
    }

    public FunctionDefinitionDto getF2() {
        return f2;
    }

    public void setF2(FunctionDefinitionDto f2) {
        this.f2 = f2;
    }
}
