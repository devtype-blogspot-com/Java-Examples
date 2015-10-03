import java.util.Scanner;

public class Main {
    public static void test1() {
        double[][] x = { {  1,  10,  20 },
                {  1,  20,  40 },
                {  1,  40,  15 },
                {  1,  80, 100 },
                {  1, 160,  23 },
                {  1, 200,  18 } };
        double[] y = { 243, 483, 508, 1503, 1764, 2129 };
        MultipleLinearRegression regression = new MultipleLinearRegression(x, y);

        System.out.printf("%.2f + %.2f beta1 + %.2f beta2  (R^2 = %.2f)\n",
                regression.beta(0), regression.beta(1), regression.beta(2), regression.R2());
    }

    public static void test2() {
        double[][] x = { {  4,  2 },
                {  5,  2 },
                {  2,  6 },
                {  3,  0 } };
        double[] y = { 8, 4, 2, 8 };
        MultipleLinearRegression regression = new MultipleLinearRegression(x, y);

        System.out.printf("%f + %f beta1  (R^2 = %.2f)\n",
                regression.beta(0), regression.beta(1), regression.R2());
    }

    public static void main(String[] args) {
        // Ввод данных
        Scanner s = new Scanner(System.in);
        int n = s.nextInt();
        int m = s.nextInt();
        double[][] x = new double[n][m];
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = new double[m];
            for (int j = 0; j < m; j++) {
                x[i][j] = s.nextDouble();
            }
            y[i] = s.nextDouble();
        }

        MultipleLinearRegression regression = new MultipleLinearRegression(x, y);

        for (int i = 0; i < m; i++) {
            System.out.print(regression.beta(i));
            if (i != n - 1) {
                System.out.print(" ");
            }
        }
    }
}

class MultipleLinearRegression {
    private final int N;        // number of
    private final int p;        // number of dependent variables
    private final Matrix beta;  // regression coefficients
    private double SSE;         // sum of squared
    private double SST;         // sum of squared

    public MultipleLinearRegression(double[][] x, double[] y) {
        if (x.length != y.length) throw new RuntimeException("dimensions don't agree");
        N = y.length;
        p = x[0].length;

        Matrix X = new Matrix(x);

        // create matrix from vector
        Matrix Y = new Matrix(y, N);

        // find least squares solution
        QRDecomposition qr = new QRDecomposition(X);
        beta = qr.solve(Y);


        // mean of y[] values
        double sum = 0.0;
        for (int i = 0; i < N; i++)
            sum += y[i];
        double mean = sum / N;

        // total variation to be accounted for
        for (int i = 0; i < N; i++) {
            double dev = y[i] - mean;
            SST += dev*dev;
        }

        // variation not accounted for
        Matrix residuals = X.times(beta).minus(Y);
        SSE = residuals.norm2() * residuals.norm2();

    }

    public double beta(int j) {
        return beta.get(j, 0);
    }

    public double R2() {
        return 1.0 - SSE/SST;
    }
}

    class Matrix implements Cloneable, java.io.Serializable {
        private double[][] A;

        private int m, n;

        public Matrix (double vals[], int m) {
            this.m = m;
            n = (m != 0 ? vals.length/m : 0);
            if (m*n != vals.length) {
                throw new IllegalArgumentException("Array length must be a multiple of m.");
            }
            A = new double[m][n];
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    A[i][j] = vals[i+j*m];
                }
            }
        }

        public Matrix (double[][] A, int m, int n) {
            this.A = A;
            this.m = m;
            this.n = n;
        }

        public Matrix (int m, int n) {
            this.m = m;
            this.n = n;
            A = new double[m][n];
        }

        public Matrix (double[][] A) {
            m = A.length;
            n = A[0].length;
            for (int i = 0; i < m; i++) {
                if (A[i].length != n) {
                    throw new IllegalArgumentException("All rows must have the same length.");
                }
            }
            this.A = A;
        }

        public Matrix times (Matrix B) {
            if (B.m != n) {
                throw new IllegalArgumentException("Matrix inner dimensions must agree.");
            }
            Matrix X = new Matrix(m,B.n);
            double[][] C = X.getArray();
            double[] Bcolj = new double[n];
            for (int j = 0; j < B.n; j++) {
                for (int k = 0; k < n; k++) {
                    Bcolj[k] = B.A[k][j];
                }
                for (int i = 0; i < m; i++) {
                    double[] Arowi = A[i];
                    double s = 0;
                    for (int k = 0; k < n; k++) {
                        s += Arowi[k]*Bcolj[k];
                    }
                    C[i][j] = s;
                }
            }
            return X;
        }

        public double[][] getArray () {
            return A;
        }

        public Matrix minus (Matrix B) {
            checkMatrixDimensions(B);
            Matrix X = new Matrix(m,n);
            double[][] C = X.getArray();
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    C[i][j] = A[i][j] - B.A[i][j];
                }
            }
            return X;
        }

        private void checkMatrixDimensions (Matrix B) {
            if (B.m != m || B.n != n) {
                throw new IllegalArgumentException("Matrix dimensions must agree.");
            }
        }

        public double get (int i, int j) {
            return A[i][j];
        }

        public double norm2 () {
            return (new SingularValueDecomposition(this).norm2());
        }

        public double[][] getArrayCopy () {
            double[][] C = new double[m][n];
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    C[i][j] = A[i][j];
                }
            }
            return C;
        }

        public int getRowDimension () {
            return m;
        }

        public int getColumnDimension () {
            return n;
        }

        public Matrix getMatrix (int i0, int i1, int j0, int j1) {
            Matrix X = new Matrix(i1-i0+1,j1-j0+1);
            double[][] B = X.getArray();
            try {
                for (int i = i0; i <= i1; i++) {
                    for (int j = j0; j <= j1; j++) {
                        B[i-i0][j-j0] = A[i][j];
                    }
                }
            } catch(ArrayIndexOutOfBoundsException e) {
                throw new ArrayIndexOutOfBoundsException("Submatrix indices");
            }
            return X;
        }
    }

class SingularValueDecomposition implements java.io.Serializable {
    private double[][] U, V;
    private double[] s;
    private int m, n;

    public SingularValueDecomposition (Matrix Arg) {

        // Derived from LINPACK code.
        // Initialize.
        double[][] A = Arg.getArrayCopy();
        m = Arg.getRowDimension();
        n = Arg.getColumnDimension();

      /* Apparently the failing cases are only a proper subset of (m<n),
	 so let's not throw error.  Correct fix to come later?
      if (m<n) {
	  throw new IllegalArgumentException("Jama SVD only works for m >= n"); }
      */
        int nu = Math.min(m,n);
        s = new double [Math.min(m+1,n)];
        U = new double [m][nu];
        V = new double [n][n];
        double[] e = new double [n];
        double[] work = new double [m];
        boolean wantu = true;
        boolean wantv = true;

        // Reduce A to bidiagonal form, storing the diagonal elements
        // in s and the super-diagonal elements in e.

        int nct = Math.min(m-1,n);
        int nrt = Math.max(0,Math.min(n-2,m));
        for (int k = 0; k < Math.max(nct,nrt); k++) {
            if (k < nct) {

                // Compute the transformation for the k-th column and
                // place the k-th diagonal in s[k].
                // Compute 2-norm of k-th column without under/overflow.
                s[k] = 0;
                for (int i = k; i < m; i++) {
                    s[k] = Maths.hypot(s[k],A[i][k]);
                }
                if (s[k] != 0.0) {
                    if (A[k][k] < 0.0) {
                        s[k] = -s[k];
                    }
                    for (int i = k; i < m; i++) {
                        A[i][k] /= s[k];
                    }
                    A[k][k] += 1.0;
                }
                s[k] = -s[k];
            }
            for (int j = k+1; j < n; j++) {
                if ((k < nct) & (s[k] != 0.0))  {

                    // Apply the transformation.

                    double t = 0;
                    for (int i = k; i < m; i++) {
                        t += A[i][k]*A[i][j];
                    }
                    t = -t/A[k][k];
                    for (int i = k; i < m; i++) {
                        A[i][j] += t*A[i][k];
                    }
                }

                // Place the k-th row of A into e for the
                // subsequent calculation of the row transformation.

                e[j] = A[k][j];
            }
            if (wantu & (k < nct)) {

                // Place the transformation in U for subsequent back
                // multiplication.

                for (int i = k; i < m; i++) {
                    U[i][k] = A[i][k];
                }
            }
            if (k < nrt) {

                // Compute the k-th row transformation and place the
                // k-th super-diagonal in e[k].
                // Compute 2-norm without under/overflow.
                e[k] = 0;
                for (int i = k+1; i < n; i++) {
                    e[k] = Maths.hypot(e[k],e[i]);
                }
                if (e[k] != 0.0) {
                    if (e[k+1] < 0.0) {
                        e[k] = -e[k];
                    }
                    for (int i = k+1; i < n; i++) {
                        e[i] /= e[k];
                    }
                    e[k+1] += 1.0;
                }
                e[k] = -e[k];
                if ((k+1 < m) & (e[k] != 0.0)) {

                    // Apply the transformation.

                    for (int i = k+1; i < m; i++) {
                        work[i] = 0.0;
                    }
                    for (int j = k+1; j < n; j++) {
                        for (int i = k+1; i < m; i++) {
                            work[i] += e[j]*A[i][j];
                        }
                    }
                    for (int j = k+1; j < n; j++) {
                        double t = -e[j]/e[k+1];
                        for (int i = k+1; i < m; i++) {
                            A[i][j] += t*work[i];
                        }
                    }
                }
                if (wantv) {

                    // Place the transformation in V for subsequent
                    // back multiplication.

                    for (int i = k+1; i < n; i++) {
                        V[i][k] = e[i];
                    }
                }
            }
        }

        // Set up the final bidiagonal matrix or order p.

        int p = Math.min(n,m+1);
        if (nct < n) {
            s[nct] = A[nct][nct];
        }
        if (m < p) {
            s[p-1] = 0.0;
        }
        if (nrt+1 < p) {
            e[nrt] = A[nrt][p-1];
        }
        e[p-1] = 0.0;

        // If required, generate U.

        if (wantu) {
            for (int j = nct; j < nu; j++) {
                for (int i = 0; i < m; i++) {
                    U[i][j] = 0.0;
                }
                U[j][j] = 1.0;
            }
            for (int k = nct-1; k >= 0; k--) {
                if (s[k] != 0.0) {
                    for (int j = k+1; j < nu; j++) {
                        double t = 0;
                        for (int i = k; i < m; i++) {
                            t += U[i][k]*U[i][j];
                        }
                        t = -t/U[k][k];
                        for (int i = k; i < m; i++) {
                            U[i][j] += t*U[i][k];
                        }
                    }
                    for (int i = k; i < m; i++ ) {
                        U[i][k] = -U[i][k];
                    }
                    U[k][k] = 1.0 + U[k][k];
                    for (int i = 0; i < k-1; i++) {
                        U[i][k] = 0.0;
                    }
                } else {
                    for (int i = 0; i < m; i++) {
                        U[i][k] = 0.0;
                    }
                    U[k][k] = 1.0;
                }
            }
        }

        // If required, generate V.

        if (wantv) {
            for (int k = n-1; k >= 0; k--) {
                if ((k < nrt) & (e[k] != 0.0)) {
                    for (int j = k+1; j < nu; j++) {
                        double t = 0;
                        for (int i = k+1; i < n; i++) {
                            t += V[i][k]*V[i][j];
                        }
                        t = -t/V[k+1][k];
                        for (int i = k+1; i < n; i++) {
                            V[i][j] += t*V[i][k];
                        }
                    }
                }
                for (int i = 0; i < n; i++) {
                    V[i][k] = 0.0;
                }
                V[k][k] = 1.0;
            }
        }

        // Main iteration loop for the singular values.

        int pp = p-1;
        int iter = 0;
        double eps = Math.pow(2.0,-52.0);
        double tiny = Math.pow(2.0,-966.0);
        while (p > 0) {
            int k,kase;

            // Here is where a test for too many iterations would go.

            // This section of the program inspects for
            // negligible elements in the s and e arrays.  On
            // completion the variables kase and k are set as follows.

            // kase = 1     if s(p) and e[k-1] are negligible and k<p
            // kase = 2     if s(k) is negligible and k<p
            // kase = 3     if e[k-1] is negligible, k<p, and
            //              s(k), ..., s(p) are not negligible (qr step).
            // kase = 4     if e(p-1) is negligible (convergence).

            for (k = p-2; k >= -1; k--) {
                if (k == -1) {
                    break;
                }
                if (Math.abs(e[k]) <=
                        tiny + eps*(Math.abs(s[k]) + Math.abs(s[k+1]))) {
                    e[k] = 0.0;
                    break;
                }
            }
            if (k == p-2) {
                kase = 4;
            } else {
                int ks;
                for (ks = p-1; ks >= k; ks--) {
                    if (ks == k) {
                        break;
                    }
                    double t = (ks != p ? Math.abs(e[ks]) : 0.) +
                            (ks != k+1 ? Math.abs(e[ks-1]) : 0.);
                    if (Math.abs(s[ks]) <= tiny + eps*t)  {
                        s[ks] = 0.0;
                        break;
                    }
                }
                if (ks == k) {
                    kase = 3;
                } else if (ks == p-1) {
                    kase = 1;
                } else {
                    kase = 2;
                    k = ks;
                }
            }
            k++;

            // Perform the task indicated by kase.

            switch (kase) {

                // Deflate negligible s(p).

                case 1: {
                    double f = e[p-2];
                    e[p-2] = 0.0;
                    for (int j = p-2; j >= k; j--) {
                        double t = Maths.hypot(s[j],f);
                        double cs = s[j]/t;
                        double sn = f/t;
                        s[j] = t;
                        if (j != k) {
                            f = -sn*e[j-1];
                            e[j-1] = cs*e[j-1];
                        }
                        if (wantv) {
                            for (int i = 0; i < n; i++) {
                                t = cs*V[i][j] + sn*V[i][p-1];
                                V[i][p-1] = -sn*V[i][j] + cs*V[i][p-1];
                                V[i][j] = t;
                            }
                        }
                    }
                }
                break;

                // Split at negligible s(k).

                case 2: {
                    double f = e[k-1];
                    e[k-1] = 0.0;
                    for (int j = k; j < p; j++) {
                        double t = Maths.hypot(s[j],f);
                        double cs = s[j]/t;
                        double sn = f/t;
                        s[j] = t;
                        f = -sn*e[j];
                        e[j] = cs*e[j];
                        if (wantu) {
                            for (int i = 0; i < m; i++) {
                                t = cs*U[i][j] + sn*U[i][k-1];
                                U[i][k-1] = -sn*U[i][j] + cs*U[i][k-1];
                                U[i][j] = t;
                            }
                        }
                    }
                }
                break;

                // Perform one qr step.

                case 3: {

                    // Calculate the shift.

                    double scale = Math.max(Math.max(Math.max(Math.max(
                                    Math.abs(s[p-1]),Math.abs(s[p-2])),Math.abs(e[p-2])),
                            Math.abs(s[k])),Math.abs(e[k]));
                    double sp = s[p-1]/scale;
                    double spm1 = s[p-2]/scale;
                    double epm1 = e[p-2]/scale;
                    double sk = s[k]/scale;
                    double ek = e[k]/scale;
                    double b = ((spm1 + sp)*(spm1 - sp) + epm1*epm1)/2.0;
                    double c = (sp*epm1)*(sp*epm1);
                    double shift = 0.0;
                    if ((b != 0.0) | (c != 0.0)) {
                        shift = Math.sqrt(b*b + c);
                        if (b < 0.0) {
                            shift = -shift;
                        }
                        shift = c/(b + shift);
                    }
                    double f = (sk + sp)*(sk - sp) + shift;
                    double g = sk*ek;

                    // Chase zeros.

                    for (int j = k; j < p-1; j++) {
                        double t = Maths.hypot(f,g);
                        double cs = f/t;
                        double sn = g/t;
                        if (j != k) {
                            e[j-1] = t;
                        }
                        f = cs*s[j] + sn*e[j];
                        e[j] = cs*e[j] - sn*s[j];
                        g = sn*s[j+1];
                        s[j+1] = cs*s[j+1];
                        if (wantv) {
                            for (int i = 0; i < n; i++) {
                                t = cs*V[i][j] + sn*V[i][j+1];
                                V[i][j+1] = -sn*V[i][j] + cs*V[i][j+1];
                                V[i][j] = t;
                            }
                        }
                        t = Maths.hypot(f,g);
                        cs = f/t;
                        sn = g/t;
                        s[j] = t;
                        f = cs*e[j] + sn*s[j+1];
                        s[j+1] = -sn*e[j] + cs*s[j+1];
                        g = sn*e[j+1];
                        e[j+1] = cs*e[j+1];
                        if (wantu && (j < m-1)) {
                            for (int i = 0; i < m; i++) {
                                t = cs*U[i][j] + sn*U[i][j+1];
                                U[i][j+1] = -sn*U[i][j] + cs*U[i][j+1];
                                U[i][j] = t;
                            }
                        }
                    }
                    e[p-2] = f;
                    iter = iter + 1;
                }
                break;

                // Convergence.

                case 4: {

                    // Make the singular values positive.

                    if (s[k] <= 0.0) {
                        s[k] = (s[k] < 0.0 ? -s[k] : 0.0);
                        if (wantv) {
                            for (int i = 0; i <= pp; i++) {
                                V[i][k] = -V[i][k];
                            }
                        }
                    }

                    // Order the singular values.

                    while (k < pp) {
                        if (s[k] >= s[k+1]) {
                            break;
                        }
                        double t = s[k];
                        s[k] = s[k+1];
                        s[k+1] = t;
                        if (wantv && (k < n-1)) {
                            for (int i = 0; i < n; i++) {
                                t = V[i][k+1]; V[i][k+1] = V[i][k]; V[i][k] = t;
                            }
                        }
                        if (wantu && (k < m-1)) {
                            for (int i = 0; i < m; i++) {
                                t = U[i][k+1]; U[i][k+1] = U[i][k]; U[i][k] = t;
                            }
                        }
                        k++;
                    }
                    iter = 0;
                    p--;
                }
                break;
            }
        }
    }

    public double norm2 () {
        return s[0];
    }
}

class Maths {
    public static double hypot(double a, double b) {
        double r;
        if (Math.abs(a) > Math.abs(b)) {
            r = b/a;
            r = Math.abs(a)*Math.sqrt(1+r*r);
        } else if (b != 0) {
            r = a/b;
            r = Math.abs(b)*Math.sqrt(1+r*r);
        } else {
            r = 0.0;
        }
        return r;
    }
}

class QRDecomposition implements java.io.Serializable {
    private double[][] QR;
    private int m, n;
    private double[] Rdiag;

    public QRDecomposition (Matrix A) {
        // Initialize.
        QR = A.getArrayCopy();
        m = A.getRowDimension();
        n = A.getColumnDimension();
        Rdiag = new double[n];

        // Main loop.
        for (int k = 0; k < n; k++) {
            // Compute 2-norm of k-th column without under/overflow.
            double nrm = 0;
            for (int i = k; i < m; i++) {
                nrm = Maths.hypot(nrm,QR[i][k]);
            }

            if (nrm != 0.0) {
                // Form k-th Householder vector.
                if (QR[k][k] < 0) {
                    nrm = -nrm;
                }
                for (int i = k; i < m; i++) {
                    QR[i][k] /= nrm;
                }
                QR[k][k] += 1.0;

                // Apply transformation to remaining columns.
                for (int j = k+1; j < n; j++) {
                    double s = 0.0;
                    for (int i = k; i < m; i++) {
                        s += QR[i][k]*QR[i][j];
                    }
                    s = -s/QR[k][k];
                    for (int i = k; i < m; i++) {
                        QR[i][j] += s*QR[i][k];
                    }
                }
            }
            Rdiag[k] = -nrm;
        }
    }

    public Matrix solve (Matrix B) {
        if (B.getRowDimension() != m) {
            throw new IllegalArgumentException("Matrix row dimensions must agree.");
        }
        if (!this.isFullRank()) {
            throw new RuntimeException("Matrix is rank deficient.");
        }

        // Copy right hand side
        int nx = B.getColumnDimension();
        double[][] X = B.getArrayCopy();

        // Compute Y = transpose(Q)*B
        for (int k = 0; k < n; k++) {
            for (int j = 0; j < nx; j++) {
                double s = 0.0;
                for (int i = k; i < m; i++) {
                    s += QR[i][k]*X[i][j];
                }
                s = -s/QR[k][k];
                for (int i = k; i < m; i++) {
                    X[i][j] += s*QR[i][k];
                }
            }
        }
        // Solve R*X = Y;
        for (int k = n-1; k >= 0; k--) {
            for (int j = 0; j < nx; j++) {
                X[k][j] /= Rdiag[k];
            }
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < nx; j++) {
                    X[i][j] -= X[k][j]*QR[i][k];
                }
            }
        }
        return (new Matrix(X,n,nx).getMatrix(0,n-1,0,nx-1));
    }

    public boolean isFullRank () {
        for (int j = 0; j < n; j++) {
            if (Rdiag[j] == 0)
                return false;
        }
        return true;
    }
}

/*

Напишите программу, которая находит наилучшее решение системы линейных алгебраических уравнений методом наименьших квадратов.

Формат входных данных:
В первой строке задаются два числа: количество уравнений n и количество неизвестных m.
Количество уравнений не меньше количества неизвестных. Далее идут n строк, каждая из которых содержит m+1 число.
Первые m чисел -- это коэффициенты i-го уравнения системы, а последнее, (m+1)-е число -- коэффициент bi, стоящий в правой части i-го уравнения.

Формат выходных данных:
В качестве результата следует вывести решение системы в виде m чисел, разделенных пробелом.

Sample Input:
4 2
4 2 8
5 2 4
2 6 2
3 0 8

Sample Output:
1.6531165311653115 -0.30894308943089427

*/

/*
http://introcs.cs.princeton.edu/java/95linear/
http://introcs.cs.princeton.edu/java/97data/MultipleLinearRegression.java.html
http://math.nist.gov/javanumerics/jama/
*/