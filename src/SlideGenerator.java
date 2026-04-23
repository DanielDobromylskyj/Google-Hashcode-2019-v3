import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import it.unimi.dsi.fastutil.ints.*;

public class SlideGenerator {
    private final List<Photo> verticalPhotos = new ArrayList<>();
    private final List<Photo> allCombinedPhotos = new ArrayList<>();

    private final List<Photo> slideshow = new ArrayList<>();

    private List<IntList> verticalTagToPhoto = new ArrayList<>();
    private List<IntList> combinedTagToPhoto = new ArrayList<>();

    private final boolean verbose;
    private final boolean use_tag_clipping;

    private void loadFromFile(Scanner fileReader) {
        int expectPhotoCount = Integer.parseInt(fileReader.nextLine());
        int verticalCount = 0;
        int horizontalCount = 0;

        while (fileReader.hasNextLine()) {
            String data = fileReader.nextLine();

            if (Photo.isVertical(data)) {
                verticalPhotos.add(new VerticalPhoto(data, verticalCount));
                verticalCount++;
            } else {
                allCombinedPhotos.add(new HorizontalPhoto(data, horizontalCount));
                horizontalCount++;
            }
        }

        if (expectPhotoCount != (verticalCount + horizontalCount)) {
            if (verbose) {
                System.out.println("WARNING: Files photo count does not match the number of records");
            }
        }
        if (verbose) {
            System.out.printf("Loaded %d Vertical Photos and %d Horizontal Photos\n", verticalCount, horizontalCount);
        }
    }

    public List<Photo> generateSlideShow() {
        this.verticalTagToPhoto = this.createTagToPhotoLookup(verticalPhotos, false);

        if (verbose) {
            System.out.println("Created Vertical Tag-To-Photo-Lookup (" + this.verticalTagToPhoto.size() + " Unique Tags)");
        }

        int unusedPhotoCount = this.pairVerticalPhotos();
        if (verbose) {
            System.out.println("Paired Vertical Photos, Leaving " + unusedPhotoCount + " photos unpaired");
        }

        this.combinedTagToPhoto = this.createTagToPhotoLookup(allCombinedPhotos, this.use_tag_clipping);
        if (verbose) {
            System.out.println("Created updated Tag-To-Photo-Lookup for all photos (" + this.combinedTagToPhoto.size() + " Unique Tags)");
        }

        int unused = this.createSlideshow();
        if (verbose) {
            System.out.println("Created Slideshow, Leaving " + unused + " photos / pairs unused.");
        }

        return this.slideshow;
    }

    public static int scoreTransition(Photo a, Photo b) {
        IntList ta = a.getTagIDs();
        IntList tb = b.getTagIDs();

        int common = countCommon(ta, tb);

        int onlyA = ta.size() - common;
        int onlyB = tb.size() - common;

        return Math.min(common, Math.min(onlyA, onlyB));
    }

    public static int scoreSlideShow(List<Photo> slideshow) {
        int score = 0;

        for (int i = 0; i < slideshow.size() - 1; i++) {
            Photo a = slideshow.get(i);
            Photo b = slideshow.get(i + 1);

            score += SlideGenerator.scoreTransition(a, b);
        }

        return score;
    }

    private List<IntList> createEmptyLookupMap(List<Photo> photoList) {
        List<IntList> lookup = new ArrayList<>();

        if (photoList.isEmpty()) {
            return lookup;
        }

        int maxTagID = -1;
        for (Photo photo : photoList) {
            for (int tagID : photo.getTagIDs()) {
                if (tagID > maxTagID) {
                    maxTagID = tagID;
                }
            }
        }

        for (int i = 0; i <= maxTagID; i++) {
            lookup.add(new IntArrayList());
        }

        return lookup;
    }

    private List<IntList> createTagToPhotoLookup(List<Photo> photoList, boolean use_tag_clipping) {
        List<IntList> lookup = createEmptyLookupMap(photoList);

        for (Photo photo : photoList) {
            int photoID = photo.getPhotoID();

            for (int tagID : photo.getTagIDs()) {
                lookup.get(tagID).add(photoID);
            }
        }


        return lookup;
    }

    public static int countCommon(IntList a, IntList b) {
        // Two-pointer merge scan - Should run in O(a.size() + b.size()), Since all tags are sorted*.   *I hope
        // This has been slightly optimised to reduce the amount indexing done,
        // Profiler recounts it saves ~1s over all datasets

        int i = 0;
        int j = 0;
        int count = 0;

        int sizeA = a.size();
        int sizeB = b.size();

        int va = a.getInt(i);
        int vb = b.getInt(j);

        while (i < sizeA && j < sizeB) {
            if (va == vb) {
                count++;
                i++;
                j++;
                if (i < sizeA) va = a.getInt(i);
                if (j < sizeB) vb = b.getInt(j);
            } else if (va < vb) {
                i++;
                if (i < sizeA) va = a.getInt(i);
            } else {
                j++;
                if (j < sizeB) vb = b.getInt(j);
            }
        }

        return count;
    }

    private VerticalPhoto findBestVerticalPair(Photo next) {
        int lowestMatchCount = 10000;
        VerticalPhoto bestPair = null;

        for (int tagID : next.getTagIDs()) {
            for (int photoID : this.verticalTagToPhoto.get(tagID)) {
                VerticalPhoto possiblePair = (VerticalPhoto) verticalPhotos.get(photoID);

                if (possiblePair.used) { continue; }
                if (photoID == next.getPhotoID()) { continue; }

                int matchCount = countCommon(next.getTagIDs(), possiblePair.getTagIDs());

                if (matchCount < lowestMatchCount) {
                    lowestMatchCount = matchCount;
                    bestPair = possiblePair;

                    if (lowestMatchCount <= 1) {
                        return bestPair; // Cant get any lower than 0/1, so we just stop searching here
                    }
                }
            }
        }

        return bestPair;
    }

    private Photo getUnusedPhoto(List<Photo> targets, Photo ignoredPhoto) {
        // This function could be optimised by remembering the last location we searched, but, It's very rarely called.
        // ~4.6s worth of compute across all datasets

        for (Photo target : targets) {
            if (target == ignoredPhoto) { continue; }
            if (target.used) { continue; }

            return target;
        }

        return null;
    }

    private int pairVerticalPhotos() {
        int combinedPhotoCount = allCombinedPhotos.size();
        int photosLeft = verticalPhotos.size();

        while (photosLeft > 1) { // Must be greater than 1, so we can actually match 2
            VerticalPhoto next = (VerticalPhoto) getUnusedPhoto(verticalPhotos, null);
            assert next != null;

            VerticalPhoto pair = findBestVerticalPair(next);

            if (pair == null) {
                pair = (VerticalPhoto) getUnusedPhoto(verticalPhotos, next);
                assert pair != null;
            }

            assert !next.used;
            assert !pair.used;

            next.used = true;
            pair.used = true;
            photosLeft -= 2;

            Photo combined = new HorizontalPhoto(next, pair, combinedPhotoCount); // Combine both, Union the tags
            allCombinedPhotos.add(combined);
            combinedPhotoCount++;
        }

        return photosLeft;
    }


    private HorizontalPhoto findBestCombinedPair(HorizontalPhoto current) {
        IntOpenHashSet seen = new IntOpenHashSet(allCombinedPhotos.size());

        int theoreticalMax = current.getTagIDs().size() / 2; // Can't be larger than this due to the min of shared tags

        int bestScore = 0;
        HorizontalPhoto bestPair = null;
        for (int tagID : current.getTagIDs()) {
            for (int photoID : this.combinedTagToPhoto.get(tagID)) {
                if (!seen.add(photoID)) continue;

                HorizontalPhoto possiblePair = (HorizontalPhoto) allCombinedPhotos.get(photoID);

                if (possiblePair.used) { continue; }
                if (photoID == current.getPhotoID()) { continue; }

                int score = scoreTransition(current, possiblePair);

                if (score > bestScore) {
                    bestScore = score;
                    bestPair = possiblePair;

                    if (bestScore >= theoreticalMax) {
                        return bestPair; // Cant be better, no matter how long we search
                    }
                }
            }
        }
        return bestPair;
    }

    private int createSlideshow() {
        int photosLeft = allCombinedPhotos.size();

        HorizontalPhoto current = (HorizontalPhoto) getUnusedPhoto(allCombinedPhotos, null);
        assert current != null;
        this.slideshow.add(current);
        photosLeft--;

        while (photosLeft > 0) { // Must be greater than 1, so we can actually match 2
            HorizontalPhoto next = this.findBestCombinedPair(current);

            if (next == null) {
                next = (HorizontalPhoto) getUnusedPhoto(allCombinedPhotos, current);
                assert next != null;
            }

            assert !next.used;
            next.used = true;
            photosLeft--;

            this.slideshow.add(next);
            current = next;
        }

        return photosLeft;
    }

    SlideGenerator(String file_path, boolean verbose, boolean use_tag_clipping) throws FileNotFoundException {
        File fileObj = new File(file_path);
        this.verbose = verbose;
        this.use_tag_clipping = use_tag_clipping;

        // Reset everything
        this.slideshow.clear();
        this.combinedTagToPhoto.clear();
        this.verticalPhotos.clear();
        this.allCombinedPhotos.clear();

        try (Scanner fileReader = new Scanner(fileObj)) {
            this.loadFromFile(fileReader);
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            throw e;
        }
    }
}
