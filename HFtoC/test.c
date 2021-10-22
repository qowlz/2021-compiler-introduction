#include <stdio.h>
#include <stdlib.h>
int main() {
	printf("Hello World!\n");
	system("dir /b");
	printf("delete test.txt\n");
	system("del test.txt");
	FILE *fp = popen("dir", "r");
	FILE *save = fopen("res", "w");
	char buffer[1024];
	while (fgets(buffer, sizeof(buffer), fp))
		fputs(buffer, save);
	pclose(fp);
	fclose(save);
	printf("*********test.txt*********\n");
	system("type ./test.txt");
	return 0;
}
