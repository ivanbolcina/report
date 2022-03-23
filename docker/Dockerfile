FROM amazoncorretto:15-alpine

RUN apk add gnuplot
RUN apk add graphviz
#RUN apk add --update --no-cache \
#    libgcc libstdc++ libx11 glib libxrender libxext libintl \
#    libcrypto1.1 libssl1.1 \
#    ttf-dejavu ttf-droid ttf-freefont ttf-liberation ttf-ubuntu-font-family
RUN apk add --update --no-cache \
    ttf-dejavu ttf-droid ttf-freefont ttf-liberation ttf-ubuntu-font-family


RUN echo 'http://dl-cdn.alpinelinux.org/alpine/edge/community' >> /etc/apk/repositories
RUN apk add --update --no-cache msttcorefonts-installer
RUN update-ms-fonts
RUN apk add font-misc-misc terminus-font ttf-inconsolata ttf-dejavu  font-noto  ttf-font-awesome font-noto-extra
RUN fc-cache -f && rm -rf /var/cache/*

ENV GDFONTPATH /usr/share/fonts/TTF

ENV APP_BASE /opt/app
RUN mkdir $APP_BASE
RUN mkdir $APP_BASE/tmp
COPY start.sh $APP_BASE
COPY templates $APP_BASE/templates
COPY report-1.0.0.jar $APP_BASE

WORKDIR $APP_BASE

CMD ["/bin/sh","start.sh"]