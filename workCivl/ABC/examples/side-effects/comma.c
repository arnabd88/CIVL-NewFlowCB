int * f() {
  return (void*)0;
}

int main() {
  int * p;
  int a[10];

  *(&p) = f(), &a[11];
  *p = *(f()), 5/0;
  *p=1, a[1]=1, a[2]=2, a[3]=3, a[4]=4;
}
