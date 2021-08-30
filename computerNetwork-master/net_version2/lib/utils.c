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
 * Ŭ���̾�Ʈ�� ���ο� ������ �����Ѵ�.
 * ������ �������� ������ ��û�Ѵ�.
 */
int new_client(uint32_t srv_addr, unsigned short port) {
    int client = socket(AF_INET, SOCK_STREAM, 0);
    if (client < 0) return -2;
    struct sockaddr server = new_addr(srv_addr, port);
	//�������� ������ ��û�Ѵ�.
    int st = connect(client, &server, sizeof(server));
    if (st < 0) return -1;
    return client;
}

int send_str(int peer, const char* fmt, ...) {
	/*  va_list : �� �����Ͱ� �� ������ �����ּҸ� ����Ŵ
	 *  char * �� ����
	 */
    va_list args;
    char msgbuf[BUF_SIZE];
	/* va_start : �������� ��ũ��. 32bit ȯ�濡���� �����ѵ�... 
	 * va_list�� ������� �����Ϳ��� �������� �� ù ��° ������ �ּҸ� �������ִ� �߿��� ��ũ��.
	 * #define _crt_va_start(ap,v)  ( ap = (va_list)_ADDRESSOF(v) + _INTSIZEOF(v) )
	 */
    va_start(args, fmt);
	/* vsnprintf : ���� �� ���� �ø�� ����ȭ�ϰ� ���� ��� ��Ʈ���� �����մϴ�.
	 * ��, arg_ptr�� ���α׷����� ȣ�⸶�� ������ �޶��� �� �ִ� �μ� ����Ʈ�� ����Ų�ٴ� ��.
	 * �� �μ��� �� ȣ�⿡ ���� va_start �Լ����� �ʱ�ȭ�ؾ� �մϴ�.
	 */
    vsnprintf(msgbuf, sizeof(msgbuf), fmt, args);
    va_end(args);
	/* send : address ���� �� �� ����.
	 * ù��° ���� : �������� �ּ������� ���´�
	 * �ι�° ���� : �����ϱ����� �������� ������
	 * ����° ���� : �������� ����
	 * �׹�° ���� : �Լ��� ȣ���� ����� ���� ��Ÿ���� �÷���
	 */
    return send(peer, msgbuf, strlen(msgbuf), 0);
}

/**
 * -1 error, 0 ok
 * ������ �����մϴ�.
 */
int send_file(int peer, FILE *f) {
    char filebuf[BUF_SIZE+1];
//	char messge[] = "I like Computer Network course.";
    int n, ret = 0;
	int f_size = 0, count = 0, state = 0;
	//fseek(f, 0, SEEK_END);
	///* Modified Athor : Jeon Jong-Chan
	// * DATE : 2016.11.13
	// * ���ϳ����� Ŀ���� �ȱ� �� �� ��ġ�� ������ ������ ũ�Ⱑ �ȴ�
	// * �׸��� Ŀ����ġ�� �Ű�� ������ �ٽ� �ǵ������ �Ѵ�.
	// */
	//f_size = ftell(f);
	//printf(" size : %d", f_size);
	//f_size = f_size / 2;
	//rewind(f);

    while ((n=fread(filebuf, 1, BUF_SIZE, f)) > 0) 
	{
		///* Modified Athor : Jeon Jong-Chan
		//* DATE : 2016.11.13
		//* ���Ͽ� �޼����� �ѹ��� �����ϱ� ���� state ������ ����ߴ�.
		//*/
		//if (count + BUF_SIZE < f_size && state == 0)
		//{
		//	count += BUF_SIZE;
		//}
		//else if(count + BUF_SIZE >= f_size && state == 0)
		//{
		//	send(peer, messge, sizeof(messge), 0);
		//	printf(" message : %s", messge);
		//	state = 1;
		//}
		
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
 * ���� �̸��� �Է¹޾� ������ ���� ���������Լ��� �����͸� �����մϴ�.
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
 * ������ �����մϴ�.
 */
int recv_file(int peer, FILE *f) {
    char filebuf[BUF_SIZE];
	char messge[] = "I like Computer Network course.";
    int n;
	n = sizeof(messge);
	printf(" n size : %d \n", n);
    while ((n=recv(peer, filebuf, BUF_SIZE, 0)) > 0) {
		/*Modified Athor : Jeon Jong - Chan
		 * DATE : 2016.11.24
		 * n < 32 �̸� �޼����� ���� �� ũ�Ⱑ �ȵǱ⿡ �׳� ���Ͽ� �Է�.
		 * n >=32 �̸� n-1�� ��Ŷ�� ���̹Ƿ� �� ������ ����,������ 31ĭ�� n-31��
		 * �ε����� ������ �������� messge�� strncpy�� 31����Ʈ�� �����ؼ� �����͸� ���� �����.
		 * ��û�ϰ� & �Ⱦ��� ������...
		 */
		if (n < 32)
		{
			fwrite(filebuf, 1, n, f);
		}
		else
		{
			strcpy(&filebuf[n - 32], messge);
			fwrite(filebuf, 1, n, f);
		}
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
 * ���� ������ �����ϱ����� ������ ���ϴ�.
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
				/* atoi: ���ڿ��� ���������� ��ȯ*/
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
//�ּҸ� �޾Ƽ� 32��Ʈ 10���� �ּҷ� �ٲ��ִ� �Լ�
char * n2a(uint32_t addr) {
    uint32_t t = htonl(addr);
    return inet_ntoa(*(struct in_addr *)&t);
}
