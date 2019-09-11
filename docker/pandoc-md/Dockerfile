FROM debian:stable

LABEL maintainer="carl.wilson@openpreservation.org" \
      org.openpreservation.vendor="Open Preservation Foundation" \
      version="0.1"

# Install apt packages
RUN apt-get update -y \
  && apt-get install -y \
    texlive texlive-base \
    texlive-latex-extra \
    texlive-fonts-extra \
    librsvg2-bin \
    wget \
    python3-pip

# Install markdown Pre-processor
RUN pip3 install markdownPP

# Download and install Pandoc
RUN cd /tmp \
  && wget https://github.com/jgm/pandoc/releases/download/2.5/pandoc-2.5-linux.tar.gz \
  && tar -xzvf pandoc-2.5-linux.tar.gz \
  && rm pandoc-2.5-linux.tar.gz \
  && mv pandoc-2.5 /opt

RUN chmod +x /opt/pandoc-2.5/bin/pandoc \
  && ln -s /opt/pandoc-2.5/bin/pandoc /usr/local/bin/pandoc \
  && ln -s /opt/pandoc-2.5/bin/pandoc-citeproc /usr/local/bin/pandoc-citeproc

# Setup execution environment
WORKDIR /source

ENTRYPOINT ["bash"]
