# PerfectCircle
Can you draw a PerfectCircle?

**How to use it**
Add it in your layout 

    <your.package.name.CircleDrawView
			android:id="@+id/circleDrawView"
			android:layout_width="match_parent"
			android:layout_height="match_parent"
			android:background="#000000" />
And in your Activity 

    circleDrawView.setOnSimilarityCalculatedListener(new CircleDrawView.OnSimilarityCalculatedListener() {
		@Override		
		public void onSimilarityCalculated(float similarity, boolean isClosed) {				
		if (isClosed) {
			// Your code here
		}
	}});
**Screenshots**
![one](https://sketchub.in/storage/project_files/26831/8799188.jpg)
![enter image description here](https://sketchub.in/storage/project_files/26831/4260676.jpg)
