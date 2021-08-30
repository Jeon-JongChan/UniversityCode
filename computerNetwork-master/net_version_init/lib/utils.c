#include <sys/socket.h>
#include <arpa/inet.h>
#include <stdarg.h>
#include <errno.h>
#include <string.h>
#include <stdlib.h>

#include "vars.h"
#include "utils.h"

struct sockaddr new_addr(uint32_t inaddr, unsigned short port) {
    struct sockaddr_in addr;
    memset(&addr, 0, sizeof(addr));
    addr.sin_family = AF_INET;
    addr.sin_addr.s_addr = htonl(inaddr);
    addr.sin_port = htons(port);
    return *(struct sockaddr *)&addr;
}

int new_server(uint32_t inaddr, uint16_t port, int backlog) {
    int ret = 0;
    int server;
    server = socket(AF_INET, SOCK_STREAM, 0);
    struct sockaddr addr = new_addr(inaddr, port);
    if (bind(server, &addr, sizeof(addr)) < 0) {
        return -2;
    }
    if (listen(server, backlog) < 0) {
        return -3;
    }
    return server;
}

/**
 * new client 
 * @return {int} status, -2 create socket error, -1 connect error
 * 클라이언트의 새로운 소켓을 생성한다.
 * 생성후 서버측에 접속을 요청한다.
 */
int new_client(uint32_t srv_addr, unsigned short port) {
    int client = socket(AF_INET, SOCK_STREAM, 0);
    if (client < 0) return -2;
    struct sockaddr server = new_addr(srv_addr, port);
	//서버측에 연결을 요청한다.
    int st = connect(client, &server, sizeof(server));
    if (st < 0) return -1;
    return client;
}

int send_str(int peer, const char* fmt, ...) {
	/*  va_list : 이 포인터가 각 인자의 시작주소를 가리킴
	 *  char * 로 정의
	 */
    va_list args;
    char msgbuf[BUF_SIZE];
	/* va_start : 가변인자 매크로. 32bit 환경에서만 가능한듯... 
	 * va_list로 만들어진 포인터에게 가변인자 중 첫 번째 인자의 주소를 가르쳐주는 중요한 매크로.
	 * #define _crt_va_start(ap,v)  ( ap = (va_list)_ADDRESSOF(v) + _INTSIZEOF(v) )
	 */
    va_start(args, fmt);
	/* vsnprintf : 문자 및 값의 시리즈를 형식화하고 버퍼 대상 스트링에 저장합니다.
	 * 단, arg_ptr이 프로그램에서 호출마다 개수가 달라질 수 있는 인수 리스트를 가리킨다는 점.
	 * 이 인수는 각 호출에 대해 va_start 함수에서 초기화해야 합니다.
	 */
    vsnprintf(msgbuf, sizeof(msgbuf), fmt, args);
    va_end(args);
	/* send : address 지정 할 수 없음.
	 * 첫번째 인자 : 목적지의 주소정보를 갖는다
	 * 두번째 인자 : 전송하기위한 데이터의 포인터
	 * 세번째 인자 : 데이터의 길이
	 * 네번째 인자 : 함수의 호출이 어떤일을 할지 나타내는 플래그
	 */
    return send(peer, msgbuf, strlen(msgbuf), 0);
}

/**
 * -1 error, 0 ok
 * 파일을 전송합니다.
 */
int send_file(int peer, FILE *f) {
    char filebuf[BUF_SIZE+1];
    int n, ret = 0;
    while ((n=fread(filebuf, 1, BUF_SIZE, f)) > 0) {
        int st = send(peer, filebuf, n, 0);
        if (st < 0) {
            err(1, "send file error, errno = %d, %s", errno, strerror(errno));
            ret = -1;
            break;
        } else {
            filebuf[n] = 0;
            info(1, " %d bytes sent", st);
        }
    }
    return ret;
}

/**
 *  -1 error opening file, -2 send file error, -3 close file error
 * 파일 이름을 입력받아 파일을 열고 파일전송함수에 데이터를 전달합니다.
 */
int send_path(int peer, char *file, uint32_t offset) {
    FILE *f = fopen(file, "rb");
    if (f) {
        fseek(f, offset, SEEK_SET);
        int st = send_file(peer, f);
        if (st < 0) {
            return -2;
        }
    } else {
        return -1;
    }
    int ret = fclose(f);
    return ret == 0 ? 0 : -3;
}
/*
 * 파일을 저장합니다.
 */
int recv_file(int peer, FILE *f) {
    char filebuf[BUF_SIZE];
    int n;
    while ((n=recv(peer, filebuf, BUF_SIZE, 0)) > 0) {
        fwrite(filebuf, 1, n, f);
    }
    return n;
}

/**
 * recv file by file path
 * @param {int} peer, peer socket
 * @param {char *} file path
 * @param {int} offset
 * @return {int} status, 
 *              -1 means recv_file error, 
 *              -2 means file open failure, 
 *              EOF means close file error
 * 
 */
/*
 * 받은 파일을 저장하기위한 파일을 엽니다.
 */
int recv_path(int peer, char *file, uint32_t offset) {
    FILE *f = fopen(file, "wb");
    if (!f) return -2;
    fseek(f, offset, SEEK_SET);
    int st = recv_file(peer, f);
    int cl = fclose(f);
    return st < 0 ? st : cl;
}

int parse_number(const char *buf, uint32_t *number) {
    int f = -1, i;
    char tmp[BUF_SIZE] = {0};
    int ret = -1;
    for (i=0; buf[i]!=0 && i<BUF_SIZE; i++) {
        if (!isdigit(buf[i])) {
            if (f >= 0) {
                memcpy(tmp, &buf[f], i-f);
                tmp[i-f] = 0;
				/* atoi: 문자열을 정수형으로 변환*/
                *number = atoi(tmp);
                ret = 0;
                f = -1;
                break;
            }
        } else {
            if (f < 0) {
                f = i;
            }
        }
    }
    return ret;
}

int parse_addr_port(const char *buf, uint32_t *addr, uint16_t *port) {
    int i;
    *addr = *port = 0;
    int f = -1;
    char tmp[BUF_SIZE] = {0};
    int cnt = 0;
    int portcnt = 0;
    for(i=0; buf[i]!=0 && i<BUF_SIZE; i++) {
        if(!isdigit(buf[i])) {
            if (f >= 0) {
                memcpy(tmp, &buf[f], i-f);
                tmp[i-f] = 0;
                if (cnt < 4) {
                    *addr = (*addr << 8) + (0xff & atoi(tmp));
                    cnt++;
                } else if (portcnt < 2) {
                    *port = (*port << 8) + (0xff & atoi(tmp));
                    portcnt++;
                } else {
                    break;
                }
                f = -1;
            }
        } else {
            if (f < 0) {
                f = i;
            }
        }
    }
    return cnt == 4 && portcnt == 2;
}

char * parse_path(const char *buf) {
    char * path = (char *)malloc(BUF_SIZE);
    int i, j;
    for (i=0; buf[i]!=' ' && i < BUF_SIZE; i++);
    if (i == BUF_SIZE) return NULL;
    i++;
    for (j=i; buf[j]!='\r' && buf[j]!= '\n' && j < BUF_SIZE; j++);
    memcpy(path, &buf[i], j-i);
    path[j-i] = 0;
    return path;
}
//주소를 받아서 32비트 10진수 주소로 바꿔주는 함수
char * n2a(uint32_t addr) {
    uint32_t t = htonl(addr);
    return inet_ntoa(*(struct in_addr *)&t);
}

