FROM postgres:17-alpine

# Build pgjwt
# Requires pgcrypto (bundled with PostgreSQL)
RUN apk add --no-cache --virtual .build-deps git make gcc musl-dev \
    && git clone --depth 1 https://github.com/michelp/pgjwt.git /tmp/pgjwt \
    && cd /tmp/pgjwt && make install \
    && apk del .build-deps \
    && rm -rf /tmp/pgjwt
