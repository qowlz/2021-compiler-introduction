// 사용한 OS: window 10
// 사용한 컴파일러: gcc 8.1.0
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

// parse된 token들을 받아 hf파일을 c파일로 만들어주는 함수
void processor(char tokens[][512], int len)
{
    FILE *output_fp = fopen("test.c", "w");
    if (output_fp == NULL)
    {
        perror("Error while opening test.c file.\n");
        exit(EXIT_FAILURE);
    }

    // 헤더파일 include 및 main함수 code 기입
    fputs("#include <stdio.h>\n#include <stdlib.h>\nint main() {\n", output_fp);

    // 명령어의 순서대로 코드 generating을 진행
    // 각 명령어는 사용하는 인자 수가 정해져있으니 사용한 인자 수만큼 idx를 증가시켜줘서 idx가 항상 keyword를 가리키도록 함.
    int idx = 0;
    while (idx < len)
    {
        char *cmd = tokens[idx];
        char content[512];
        if (strstr(cmd, "echo") != NULL)
        {
            sprintf(content, "\tprintf(\"%s\\n\");\n", tokens[idx + 1]);
            fputs(content, output_fp);

            idx += 2;
        }
        else if (strstr(cmd, "list_dir") != NULL)
        {
            fputs("\tsystem(\"dir /b\");\n", output_fp);

            idx += 1;
        }
        else if (strstr(cmd, "del") != NULL)
        {
            sprintf(content, "\tsystem(\"del %s\");\n", tokens[idx + 1]);
            fputs(content, output_fp);

            idx += 2;
        }
        else if (strstr(cmd, "show") != NULL)
        {
            sprintf(content, "\tsystem(\"type ./%s\");\n", tokens[idx + 1]);
            fputs(content, output_fp);

            idx += 2;
        }
        else if (strstr(cmd, "mov") != NULL)
        {
            sprintf(content, "\tFILE *fp = popen(\"dir\", \"r\");\n\tFILE *save = fopen(\"%s\", \"w\");\n\tchar buffer[1024];\n\twhile (fgets(buffer, sizeof(buffer), fp))\n\t\tfputs(buffer, save);\n\tpclose(fp);\n\tfclose(save);\n", tokens[idx + 2]);
            fputs(content, output_fp);

            idx += 3;
        }
    }

    // 마지막으로 return문 + 중괄호 기입
    fputs("\treturn 0;\n}\n", output_fp);
}

int main(void)
{
    FILE *input_fp = fopen("test.hf", "r");
    if (input_fp == NULL)
    {
        perror("Error while opening test.hf file.\n");
        exit(EXIT_FAILURE);
    }

    // state: 0 = normal state, 1 = inserting text state
    // normal state 일때는 alphabet과 '_'가 매칭될떄마다 buffer에 저장해두고 공백이나 )를 만날때 flush하는 방식으로 tokenize시킨다.
    // inserting text state 일때는 무슨 문자를 만나든 buffer에 넣어두고 "를 만나면 문자열 입력이 종료된것이니 flush해서 token에 저장한다.
    char ch = 0;
    int state = 0;
    char tokens[512][512] = {'\0'};
    char buffer[512] = {'\0'};
    int idx = 0;
    while ((ch = fgetc(input_fp)) != EOF)
    {
        switch (state)
        {
        case 0:
            // flush buffer when character is ' ' or ')'
            if (ch == ' ' || ch == ')')
            {
                // If buffer empty, do not add it
                if (buffer[0] == '\0') break;

                // buffer flush
                strcpy(tokens[idx++], buffer);

                // buffer reset
                memset(buffer, 0, sizeof(buffer));
            }
            // save to buffer when character is alphabet or '_'
            else if (isalpha(ch) || ch == '_')
            {
                char str[2] = {ch, '\0'};
                strcat(buffer, str);
            }
            // start inserting text state when character is "
            else if (ch == '"')
            {
                state = 1;
            }
            break;

        case 1:
            if (ch == '"')
            {
                // buffer flush & reset
                // end inserting text state
                strcpy(tokens[idx++], buffer);
                memset(buffer, 0, sizeof(buffer));
                state = 0;
            }
            else
            {
                // save any characters in buffer
                char str[2] = {ch, '\0'};
                strcat(buffer, str);
                continue;
            }
            break;
        }
    }

    processor(tokens, idx);

    fclose(input_fp);

    return 0;
}