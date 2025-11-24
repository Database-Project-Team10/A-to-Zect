package knu.atoz.mbti;

public class MbtiDimension {
    Long id;
    String dimensionType; 
    String option1; 
    String option2; 

    public MbtiDimension(Long id, String dimensionType, String option1, String option2) {
        this.id = id;
        this.dimensionType = dimensionType;
        this.option1 = option1;
        this.option2 = option2;
    }

    
    public Long getId() { return id; }
    public String getDimensionType() { return dimensionType; }
    public String getOption1() { return option1; }
    public String getOption2() { return option2; }
}