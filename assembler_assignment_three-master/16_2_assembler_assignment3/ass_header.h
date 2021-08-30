#pragma once
#include<stdio.h>
#include<string.h>
/*
 * DATE   : 2016.10.30
 * AUTHOR : JEON JONG CHAN
 */

//종료 OPCODE
#define HTL	000
//더하기 OPCODE
#define ADD	100
//뺄셈 OPCODE
#define SUB	200
//저장 OPCODE
#define STA	300
//불러오기(LOAD) OPCODE
#define LDA	500
//무조건 점프 OPCODE
#define BRA	600
//0일 때만 점프 OPCODE
#define BRZ	700
//0보다 같거나 크거나 OPCODE
#define BRP	800
//출력 OPCODE
#define INP	901
//입력 OPCODE
#define OUT	902

//입력받는 명령어를 구분하기 위해 명령어를 저장한 배열
char opcode[10][3] = {
	 {'H','L','T'}
	,{'A','D','D'}
	,{'S','U','B'}
	,{'S','T','A'}
	,{'L','D','A'}
	,{'B','R','A'}
	,{'B','R','Z'}
	,{'B','R','P'}
	,{'I','N','P'}
	,{'O','U','T'}
};