mv server/node_modules server/.node_modules
cp -rv server/* ../openshift/thingsnear/
mv server/.node_modules server/node_modules
cd ../openshift/thingsnear/
git add --all *
git commit -m 'Update'
git push
cd ../../thingsnear
