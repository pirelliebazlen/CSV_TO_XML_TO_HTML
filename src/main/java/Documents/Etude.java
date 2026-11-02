
package Documents;

import java.util.ArrayList;
import java.util.List;

public class Etude {

    private String xmlId;
    private String studyID;
    private String reportID;
    private String report;
    private String methodLabel;
    private String labels;
    private String localizations;
    private String labelsLocalizationsBySentence;
    private String labelCUIS;
    private String localizationsCUIS;

    private List<Image> images = new ArrayList<>();

    public Etude(String xmlId, String studyID, String reportID,  String report, String methodLabel, String labels,
                 String localizations, String labelsLocalizationsBySentence,  String labelCUIS, String localizationsCUIS)
    {

        this.xmlId = xmlId;
        this.studyID = studyID;
        this.reportID = reportID;
        this.report = report;
        this.methodLabel = methodLabel;
        this.labels = labels;
        this.localizations = localizations;
        this.labelsLocalizationsBySentence = labelsLocalizationsBySentence;
        this.labelCUIS = labelCUIS;
        this.localizationsCUIS = localizationsCUIS;
    }

    public String getXmlId() { return xmlId; }
    public String getStudyID() { return studyID; }
    public String getReportID() { return reportID; }
    public String getReport() { return report; }
    public String getMethodLabel() { return methodLabel; }
    public String getLabels() { return labels; }
    public String getLocalizations() { return localizations; }

    public String getLabelsLocalizationsBySentence() {
        return labelsLocalizationsBySentence;
    }

    public String getLabelCUIS() { return labelCUIS; }
    public String getLocalizationsCUIS() { return localizationsCUIS; }

    public void setReport(String report) {
        this.report = report;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public void setLocalizations(String localizations) {
        this.localizations = localizations;
    }

    public List<Image> getImages() {
        return images;
    }

    public void ajouterImage(Image image) {
        images.add(image);
    }
}
