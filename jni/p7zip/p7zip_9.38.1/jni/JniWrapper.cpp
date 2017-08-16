/*
 * JniWrapper.cpp
 *
 *  Created on: 2014-8-12
 *      Author: HZY
 */
#include <string.h>
#include <stdio.h>
#include <stdbool.h>

#include "JniWrapper.h"


#define ARGV_LEN_MAX  	256
#define ARGC_MAX  		32

/**
 * Extern function
 */
extern int MY_CDECL main(
#ifndef _WIN32
		int numArgs, const char *args[]
#endif
		);

extern const char* MY_CDECL main_GetStream(
#ifndef _WIN32
		int numArgs, char *args[]
#endif
		);

/**
 * get args from string
 */
static bool str2args(const char *s, char argv[][ARGV_LEN_MAX], int* argc) {

	bool in_token, in_container, escaped;
	bool ret;
	char container_start, c;
	int len, i;
	int index = 0;
	int arg_count = 0;

	ret = true;
	container_start = 0;
	in_token = false;
	in_container = false;
	escaped = false;

	len = strlen(s);
	for (i = 0; i < len; i++) {
		c = s[i];
		switch (c) {
		case ' ':
		case '\t':
		case '\n':
			if (!in_token)
				continue;
			if (in_container) {
				argv[arg_count][index++] = c;
				continue;
			}
			if (escaped) {
				escaped = false;
				argv[arg_count][index++] = c;
				continue;
			}
			/* if reached here, we're at end of token */
			in_token = false;
			argv[arg_count++][index] = '\0';
			index = 0;
			break;
			/* handle quotes */
		case '\'':
		case '\"':
			if (escaped) {
				argv[arg_count][index++] = c;
				escaped = false;
				continue;
			}
			if (!in_token) {
				in_token = true;
				in_container = true;
				container_start = c;
				continue;
			}
			if (in_container) {
				if (c == container_start) { //container end
					in_container = false;
					in_token = false;
					argv[arg_count++][index] = '\0';
					index = 0;
					continue;
				} else { //not the same as contain start char
					argv[arg_count][index++] = c;
					continue;
				}
			}
			LOGE("Parse Error! Bad quotes\n");
			ret = false;
			break;
		case '\\':
			if (in_container && s[i + 1] != container_start) {
				argv[arg_count][index++] = c;
				continue;
			}
			if (escaped) {
				argv[arg_count][index++] = c;
				continue;
			}
			escaped = true;
			break;
		default: //normal char
			if (!in_token) {
				in_token = true;
			}
			argv[arg_count][index++] = c;
			if (i == len - 1) { //reach the end
				argv[arg_count++][index++] = '\0';
			}
			break;
		}
	}
	*argc = arg_count;

	if (in_container) {
		LOGE("Parse Error! Still in container\n");
		ret = false;
	}
	if (escaped) {
		LOGE("Parse Error! Unused escape (\\)\n");
		ret = false;
	}
	return ret;
}

int executeCommand(const char* cmd) {
	int argc = 0;
	char temp[ARGC_MAX][ARGV_LEN_MAX] = { 0 };
	char* argv[ARGC_MAX] = { 0 };

	/** command line option error*/
	if ((str2args(cmd, temp, &argc)) == false) {
		return 7;
	}

	for (int i = 0; i < argc; i++) {
		argv[i] = temp[i];
		LOGD("arg[%d]:[%s]", i, argv[i]);
	}
	return main(argc, (const char**) argv);
}

const char* executeCommandGetStream(const char* cmd) {
	int argc = 0;
	char temp[ARGC_MAX][ARGV_LEN_MAX] = { 0 };
	char* argv[ARGC_MAX] = { 0 };

	/** command line option error*/
	if ((str2args(cmd, temp, &argc)) == false) {
		return "CommandError";
	}

	for (int i = 0; i < argc; i++) {
		argv[i] = temp[i];
		LOGI("arg[%d]:[%s]", i, argv[i]);
	}

	return main_GetStream(argc, (char**) argv);
}
