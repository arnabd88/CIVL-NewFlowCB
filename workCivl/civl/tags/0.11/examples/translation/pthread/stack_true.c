/**
* This program is taken from the Pthread benchmarks of SV-COMP 2014.
* https://svn.sosy-lab.org/software/sv-benchmarks/tags/svcomp14/
*/

#include <pthread.h>
#include <stdio.h>

#define TRUE	  (1)
#define FALSE	  (0) 
#define SIZE	  (5)
#define OVERFLOW  (-1)
#define UNDERFLOW (-2)

static int top=0;
static unsigned int arr[SIZE];
pthread_mutex_t m;
_Bool flag=FALSE;

void error(void) 
{ 
  ERROR: ;
  goto ERROR;
  return;
}

void inc_top(void)
{
  top++;
}

void dec_top(void)
{
  top--;
}

int get_top(void)
{
  return top;
}

int stack_empty(void)
{
  (top==0) ? TRUE : FALSE; 
}

int push(unsigned int *stack, int x)
{
  if (top==SIZE) 
  {
    printf("stack overflow\n");
    return OVERFLOW;
  } 
  else 
  {
    stack[get_top()] = x;
    inc_top();
  }
  return 0;
}

int pop(unsigned int *stack)
{
  if (top==0) 
  {
    printf("stack underflow\n");	
    return UNDERFLOW;
  } 
  else 
  {
    dec_top();
    return stack[get_top()];  
  }
  return 0;
}

void *t1(void *arg) 
{
  int i;
  unsigned int tmp;

  for(i=0; i<SIZE; i++)
  {
    pthread_mutex_lock(&m);   
    tmp = 1%SIZE;
    if ((push(arr,tmp)==OVERFLOW))
      error();
    pthread_mutex_unlock(&m);
  }
}

void *t2(void *arg) 
{
  int i;

  for(i=0; i<SIZE; i++)
  {
    pthread_mutex_lock(&m);
    if (top>0)
    {    
      if ((pop(arr)==UNDERFLOW))
        error();
    }    
    pthread_mutex_unlock(&m);
  }
}


int main(void) 
{
  pthread_t id1, id2;

  pthread_mutex_init(&m, 0);

  pthread_create(&id1, NULL, t1, NULL);
  pthread_create(&id2, NULL, t2, NULL);

  pthread_join(id1, NULL);
  pthread_join(id2, NULL);

  return 0;
}


