git filter-branch -f --commit-filter '
                GIT_COMMITTER_NAME="DEVTYPE";
                GIT_AUTHOR_NAME="DEVTYPE";
                GIT_COMMITTER_EMAIL="Informatik.on.GitHub@gmail.com";
                GIT_AUTHOR_EMAIL="Informatik.on.GitHub@gmail.com";
                git commit-tree "$@";
        ' HEAD
