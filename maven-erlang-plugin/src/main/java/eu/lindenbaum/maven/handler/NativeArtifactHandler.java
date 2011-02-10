package eu.lindenbaum.maven.handler;

import com.github.ghaskins.osclarity.OsInfo;
import com.github.ghaskins.osclarity.OsInfoFactory;

import org.apache.maven.artifact.handler.ArtifactHandler;

public class NativeArtifactHandler implements ArtifactHandler {

  private final String type = "erlang-otp-native";
  private String classifier;

  public NativeArtifactHandler() {
    OsInfo osinfo = OsInfoFactory.getInfo();

    classifier = osinfo.getName() + "-" + osinfo.getVersion() + "-" + osinfo.getArch();
  }

  @Override
  public String getExtension() {
    return type;
  }

  @Override
  public String getDirectory() {
    return null;
  }

  @Override
  public String getClassifier() {
    return classifier;
  }

  @Override
  public String getPackaging() {
    return type;
  }

  @Override
  public boolean isIncludesDependencies() {
    return false;
  }

  @Override
  public String getLanguage() {
    return "erlang-native-mix";
  }

  @Override
  public boolean isAddedToClasspath() {
    return false;
  }

}
