package ru.mai.mathoptimization.dto;

public class VariantDto {
    private int id;
    private String title;
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
