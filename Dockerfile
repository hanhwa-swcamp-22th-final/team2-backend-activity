FROM gradle:8.12-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre
# ActivityPackagePdfReportService 가 OpenPDF BaseFont 로 한글 폰트(NanumGothic)
# 를 직접 로드. 기본 temurin 이미지엔 한글 폰트가 없어 PDF 가 글자 깨짐으로
# 렌더되던 문제를 해소한다. fonts-nanum 이 설치되면
# /usr/share/fonts/truetype/nanum/NanumGothic.ttf 가 생성되고 서비스의
# resolveFontPath() 탐색 경로와 일치.
RUN apt-get update \
 && apt-get install -y --no-install-recommends fonts-nanum fontconfig \
 && rm -rf /var/lib/apt/lists/* \
 && fc-cache -f || true
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
USER appuser
EXPOSE 8013
ENTRYPOINT ["java", "-jar", "app.jar"]
