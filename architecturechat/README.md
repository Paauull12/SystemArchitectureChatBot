to run this you install your stuff 
then 
1. npm run compile-web
2. press f5 and a new window with the extension should appear


dumnezeu
# Stop any watch or dev servers
pkill -f webpack

# Clean up
rm -rf dist
rm -rf .vscode-test-web

# Rebuild from scratch
npm run compile-web
npm run run-in-browser