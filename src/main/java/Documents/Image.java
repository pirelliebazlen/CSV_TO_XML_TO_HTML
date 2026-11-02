package Documents;

public class Image {

    private String xmlId;
    private String imageID;
    private String imageDir;
    private String projection;
    private String pediatric;
    private String methodProjection;

    public Image(String xmlId, String imageID, String imageDir,
                 String projection, String pediatric,
                 String methodProjection)
    {
        this.xmlId = xmlId;
        this.imageID = imageID;
        this.imageDir = imageDir;
        this.projection = projection;
        this.pediatric = pediatric;
        this.methodProjection = methodProjection;
    }

    public String getXmlId() {
        return xmlId;
    }
    public String getImageID() {
        return imageID;
    }

    public void setImageID(String imageID) {
        this.imageID = imageID;
    }

    public String getImageDir() {
        return imageDir;
    }

    public void setImageDir(String imageDir) {
        this.imageDir = imageDir;
    }

    public String getProjection() {
        return projection;
    }

    public void setProjection(String projection) {
        this.projection = projection;
    }

    public String getPediatric() {
        return pediatric;
    }

    public void setPediatric(String pediatric) {
        this.pediatric = pediatric;
    }

    public String getMethodProjection() {
        return methodProjection;
    }

    public void setMethodProjection(String methodProjection) {
        this.methodProjection = methodProjection;
    }
}