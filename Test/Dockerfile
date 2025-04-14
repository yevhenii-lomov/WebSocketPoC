FROM grafana/k6

COPY ./script.js /scripts/

COPY --chmod=755 ./wait.sh /scripts/

ENTRYPOINT ["sh", "/scripts/wait.sh"]