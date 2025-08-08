{ pkgs }: {
  deps = [
    pkgs.jdk11    # Kullandığınız Java versiyonuna göre pkgs.jdk8, pkgs.jdk17 olabilir
    pkgs.gradle   # Gradle'ı yükle
  ];
}
