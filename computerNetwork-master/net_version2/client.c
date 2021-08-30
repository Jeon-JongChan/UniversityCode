#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <signal.h>
#include <string.h>

#include "vars.h"
#include "utils.h"

enum USER_CMD {
    USER_INVALID = -1,
    USER_LS = 0,
    USER_PWD,
    USER_CD,
    USER_CDUP,
    USER_RENAME,
    USER_PUT,
    USER_GET,
    USER_USER,
    USER_PASS,
    USER_TYPE,
    USER_BYE,
    USER_MKD,
    USER_DELE,
    USER_RNFR,
    USER_RNTO,
    USER_RMD,
    USER_LCD,
    USER_LLS,
    USER_LPWD,
    USER_HELP,
    USER_COUNT
};

struct ftp_cmd USER_CMD_LIST[USER_COUNT] = {
    {"LS", USER_LS},
    {"PWD", USER_PWD},
    {"CD", USER_CD},
    {"CDUP", USER_CDUP},
    {"RENAME", USER_RENAME},
    {"PUT", USER_PUT},
    {"GET", USER_GET},
    {"USER", USER_USER},
    {"PASS", USER_PASS},
    {"TYPE", USER_TYPE},
    {"BYE", USER_BYE},
    {"MKD", USER_MKD},
    {"DELE", USER_DELE},
    {"RNFR", USER_RNFR},
    {"RNTO", USER_RNTO},
    {"RMD", USER_RMD},
    {"LCD", USER_LCD},
    {"LLS", USER_LLS},
    {"LPWD", USER_LPWD},
    {"HELP", USER_HELP}
};

enum USER_CMD parse_input_cmd(char* buf, int len) {
    int i, j;
    for (i=0; i<sizeof(USER_CMD_LIST)/sizeof(USER_CMD_LIST[0]); i++) {
        for(j=0; USER_CMD_LIST[i].name[j] && j < len; j++) {
            if (USER_CMD_LIST[i].name[j] != buf[j] & 0x1f && USER_CMD_LIST[i].name[j] != buf[j]- 32) 
                break;
        }
        if (USER_CMD_LIST[i].name[j] == '\0' && (buf[j]==' ' || buf[j]==0))
            return USER_CMD_LIST[i].cmd;
    }
    return USER_INVALID;
}

enum CLIENT_STATE {
    ST_NONE,
	ST_PASS,
    ST_PASVLIST,
    ST_PASVLIST2,
    ST_PASVGET,
    ST_PASVGET2,
    ST_PASVPUT,
    ST_PASVPUT2
};

int running = 1;

void ouch() {
    running = 0;
}

int main(int argc, char *argv[]) {
    int server_port = 2121;

    if (argc < 2) {
        printf("usage: %s <addr> [2121]\n", argv[0]);
        exit(0);
    }
    if (argc == 3) {
        server_port = atoi(argv[2]);
    }
	//서버와 연결된다. 2
    int client = new_client(ntohl(inet_addr(argv[1])), server_port);
    if (client < 0) {
        err(1, "can not connect to %s %d", argv[1], server_port);
        err(1, "exit ...");
        exit(1);
    }
    int i, n;
    char buf[BUF_SIZE+1];
    char tmpbuf[BUF_SIZE+1];
    char cmdbuf[BUF_SIZE+1];
	char id[BUF_SIZE + 1];
	char password[BUF_SIZE + 1];
    int data_client = -1;
    struct sockaddr_in data_client_addr;
    uint32_t addr;
    uint16_t port;
    char path[BUF_SIZE];
    int code = -1;
    enum CLIENT_STATE state = ST_NONE;
    char filename[BUF_SIZE], line[BUF_SIZE];
	/*
	 * 클라이언트 소켓에서 메세지를 받는다. 4
	 */
    while ((n=recv(client, buf, sizeof(buf), MSG_PEEK)) > 0) {
        if (!running) break;
        for (i=0; i<n; i++) {
            if (buf[i] == '\n') break;
        }
        if (buf[i] != '\n') {
            err(1, "no line break found");
            break;
        }
		//서버에게 받은 메세지 출력  6
        n = recv(client, buf, BUF_SIZE, 0);
        buf[n] = 0;
		printf("33\n");
        printf("%s", buf);
        fflush(stdout);
		/* 모름! 교수님 설명부탁드려요!*/
        parse_number(buf, &code);
		/*
		 * 밑에서 명령어를 받은후 변경된 상태에 따라 진행되는 코드부분
		 */
        if (code < RPL_ERR_UNKWNCMD && state != ST_NONE) {
            switch(state) {
                case ST_PASVLIST:
                case ST_PASVGET:
                case ST_PASVPUT:
                    if (code == RPL_PASVOK) {
                        strcpy(tmpbuf, buf);
                        tmpbuf[0] = tmpbuf[1] = tmpbuf[2] = tmpbuf[3] = ' ';
						/*이부분도 모름!*/
                        parse_addr_port(tmpbuf, &addr, &port);
                        switch(state) {
							/*
							 * ls명령어에 의해서 변경된 상태에 따른 소스코드
							 * LIST명령어를 서버의 클라이언트 소켓에 전송한다.
							 */
                            case ST_PASVLIST:
                                send_str(client, "LIST\r\n");
                                break;
								/*
								 * get명령어에 의해서 변경된 상태에 따른 소스코드
								 * RETR명령어를 서버의 클라이언트 소켓에 전송한다.
								 */
                            case ST_PASVGET:
                                send_str(client, "RETR %s\r\n", filename);
                                break;
                            case ST_PASVPUT:
                                send_str(client, "STOR %s\r\n", filename);
                                break;
                        }
						/*
						 * 클라이언트 소켓을 생성한다.
						 * 이때 서버와 연결되있다.
						 */
                        data_client = new_client(addr, port);
						//상태 변수를 증가시킨다.
                        state++;
                    } else {
                        state = ST_NONE;
                    }
                    break;
				case ST_PASS:
					//명령어와 같이 PASS 전송
					send_str(client, "PASS %s\r\n", password);
					state = ST_NONE;
					break;
                case ST_PASVLIST2:
                case ST_PASVGET2:
                case ST_PASVPUT2:
                    if (data_client < 0) {
                        err(1, "data client not created");
                    } else {
                        if (state == ST_PASVLIST2) {
							printf("44\n");
                            recv_file(data_client, stdout);
							printf("55555555555555555\n");
                        } else if (state == ST_PASVGET2) {
							/*
							 * 서버와 연결된 소켓을 통해 파일을 받는다.
							 */
                            recv_path(data_client, filename, 0);
                        } else if (state == ST_PASVPUT2) {
                            FILE *f = fopen(filename, "rb");
                            if (f) {
                                send_file(data_client, f);
                                fclose(f);
                            } else {
                                err(1, "err open file %s", filename);
                            }
                        }
                        info(1, "closing data socket ... %d", close(data_client));
                        data_client = -1;
                        state = ST_NONE;
                    }
                    break;
                default:
                    state = ST_NONE;
                    break;
            }
            if (code < RPL_ERR_UNKWNCMD)
                continue;
        }
        if (code >= RPL_ERR_UNKWNCMD) state = ST_NONE;
        int valid = 0;
        while (!valid) {
            valid = 1;
            printf("ftp >>> ");
			/*
			 * 사용자에게서 명령어를 입력받는다.
			 */
            if (!fgets(line, BUF_SIZE, stdin)){
                running = 0;
				printf("here!!!!!\n");
                break;
            }
            int len = strlen(line);
            len --;
            while (line[len] == '\n' || line[len] == '\r') len--;
            len ++;
            line[len] = 0;
			/*
			 *입력받은 명령어를 매크로와 매칭시켜서 변환한다.
			 */
            enum USER_CMD cmd = parse_input_cmd(line, len);
            switch(cmd) {
                case USER_USER:
					/*
					 * DATE : 2016.11.04
					 * 인증 시작.
					 */
					printf("USER NAME >>> ");
					//표준입출력으로 USER ID를 받아온다.
					fgets(id, BUF_SIZE, stdin);
					/*
					 * 명령어와 같이 ID를 전송시키는데 서버측 형식에 맞춰보내야 하므로
				 	 * 명령어 대신 ID 끝에 0을 넣어준다.
					 */
					len = strlen(id);
					len--;
					while (id[len] == '\n' || id[len] == '\r') len--;
					len++;
					id[len] = 0;
					//명령어와 같이 ID 전송
					send_str(client, "USER %s\r\n",id);
                case USER_PASS:
					printf("USER PASS >>> ");
					//표준입출력으로 USER ID를 받아온다.
					fgets(password, BUF_SIZE, stdin);
					/*
					* 명령어와 같이 PASS를 전송시키는데 서버측 형식에 맞춰보내야 하므로
					* 명령어 대신 PASS 끝에 0을 넣어준다.
					*/
					len = strlen(password);
					len--;
					while (password[len] == '\n' || password[len] == '\r') len--;
					len++;
					password[len] = 0;
					state = ST_PASS;
					break;
                case USER_TYPE:
                case USER_MKD:
                case USER_DELE:
                case USER_RNFR:
                case USER_RNTO:
                case USER_RMD:
                    send_str(client, "%s\r\n", line);
                    break;
                case USER_LS:
                    send_str(client, "PASV\r\n");
                    state = ST_PASVLIST;
                    break;
                case USER_CD:
                    send_str(client, "CWD %s\r\n", &line[3]);
                    break;
                case USER_PWD:
                    send_str(client, "PWD\r\n");
                    break;
                case USER_CDUP:
                    send_str(client, "CDUP\r\n");
                    break;
                case USER_HELP:
                    for (i=0; i<sizeof(USER_CMD_LIST)/sizeof(USER_CMD_LIST[0]); i++) {
                        printf("%s\n", USER_CMD_LIST[i].name);
                    }
                    valid = 0;
                    break;
                case USER_BYE:
                    send_str(client, "QUIT\r\n");
                    running = 0;
                    break;
                case USER_LCD:
                    chdir(&line[4]);
                    valid = 0;
                    break;
                case USER_LLS:
                    getcwd(path, sizeof(path));
                    printf("%s\n", path);

                    sprintf(cmdbuf, "ls -l %s", path);
                    FILE *p2 = popen(cmdbuf, "r");
                    int n;
                    while ((n=fread(tmpbuf, 1, BUF_SIZE, p2)) > 0 ) {
                        fwrite(tmpbuf, 1, n, stdout);
                    }
                    pclose(p2);

                    valid = 0;
                    break;
                case USER_LPWD:
                    getcwd(path, sizeof(path));
                    printf("%s\n", path);
                    valid = 0;
                    break;
                case USER_GET:
					/*
					 * get 명령어를 입력 받았을때 실행된다.
					 * 우선적으로 서버의 클라이언트 소켓에 PASV 를보내서 
					 * passiv 설정을 해준다.
					 */
                    send_str(client, "PASV\r\n");
					/*
					 * 원하는 파일명을 filename에 복사한다.
					 */
                    strcpy(filename, &line[4]);
					/*
					 * 상태를 ST_PASVGET로 바꾼다.
					 * 이 상태변수로 윗부분에서 파일전송신호를 보낸다.
					 */
                    state = ST_PASVGET;
                    break;
                case USER_PUT:
                    send_str(client, "PASV\r\n");
                    strcpy(filename, &line[4]);
                    state = ST_PASVPUT;
                    break;
                default:
                    warn(1, "unknown user cmd");
                    valid = 0;
                    break;
            }
        }
        if (!running) break;
    }
    int st = close(client);
    info(1, "FTP client close socket ... %d", st);
    info(1, "FTP client shutdown");
    if (data_client > 0) {
        st = close(data_client);
        info(1, "FTP client close data socket ... %d", st);
        info(1, "FTP client data socket shutdown");
    }
    return 0;
}

