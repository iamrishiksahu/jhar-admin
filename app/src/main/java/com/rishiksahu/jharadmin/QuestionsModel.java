package com.rishiksahu.jharadmin;

public class QuestionsModel {

    public String q, a, b, c, d, correct ;
    public Long positiveGrade;
    public Double negativeGrade;

    public QuestionsModel() {

    }

    public void setQ(String q) {
        this.q = q;
    }

    public void setA(String a) {
        this.a = a;
    }

    public void setB(String b) {
        this.b = b;
    }

    public void setC(String c) {
        this.c = c;
    }

    public void setD(String d) {
        this.d = d;
    }

    public void setCorrect(String correct) {
        this.correct = correct;
    }

    public void setPositiveGrade(Long positiveGrade) {
        this.positiveGrade = positiveGrade;
    }

    public void setNegativeGrade(Double negativeGrade) {
        this.negativeGrade = negativeGrade;
    }
}
